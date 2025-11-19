package com.bbey.neez.service;

import com.bbey.neez.dto.auth.*;
import com.bbey.neez.entity.EmailVerificationToken;
import com.bbey.neez.entity.Users;
import com.bbey.neez.jwt.JwtUtil;
import com.bbey.neez.repository.EmailVerificationTokenRepository;
import com.bbey.neez.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest req) {

        if (userRepository.findByUserId(req.getUserId()).isPresent()) {
            return new AuthResponse(false, "이미 존재하는 아이디입니다.");
        }

        Users u = new Users();
        u.setUserId(req.getUserId());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        u.setCreated_at(LocalDateTime.now());
        u.setUpdated_at(LocalDateTime.now());
        u.setVerified(false);
        userRepository.save(u);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken emailToken = new EmailVerificationToken();
        emailToken.setToken(token);
        emailToken.setUser(u);
        emailToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(emailToken);

        emailService.sendVerificationEmail(u, token);

        return new AuthResponse(true, "회원가입 성공! 이메일 인증을 완료해주세요.", req.getUserId());
    }

    @Override
    public AuthResponse verifyEmail(String token) {

        EmailVerificationToken emailToken = tokenRepository.findByToken(token).orElse(null);

        if (emailToken == null) {
            return new AuthResponse(false, "유효하지 않은 인증 링크입니다.");
        }

        if (emailToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new AuthResponse(false, "인증 링크가 만료되었습니다.");
        }

        Users user = emailToken.getUser();
        user.setVerified(true);
        userRepository.save(user);

        return new AuthResponse(true, "이메일 인증이 완료되었습니다.", user.getUserId());
    }

    @Override
    public AuthResponse login(LoginRequest req) {

        Optional<Users> opt = userRepository.findByUserId(req.getUserId());
        if (!opt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 아이디입니다.");
        }

        Users user = opt.get();

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return new AuthResponse(false, "비밀번호가 일치하지 않습니다.");
        }

        if (!user.isVerified()) {
            return new AuthResponse(false, "이메일 인증이 필요합니다.");
        }

        String accessToken = jwtUtil.createAccessToken(user.getUserId());
        String refreshToken = jwtUtil.createRefreshToken(user.getUserId());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken);

        return new AuthResponse(true, "로그인 성공", tokenResponse);
    }

    @Override
    public AuthResponse logout(LogoutRequest req) {

        Optional<Users> opt = userRepository.findByUserId(req.getUserId());
        if (!opt.isPresent()) {
            return new AuthResponse(true, "로그아웃 완료");
        }

        Users user = opt.get();
        user.setRefreshToken(null);
        userRepository.save(user);

        return new AuthResponse(true, "로그아웃 완료");
    }

    @Override
    public AuthResponse delete(DeleteRequest req) {

        Optional<Users> opt = userRepository.findByUserId(req.getUserId());
        if (!opt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 사용자입니다.");
        }

        Users user = opt.get();

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return new AuthResponse(false, "비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
        return new AuthResponse(true, "회원탈퇴 완료", req.getUserId());
    }

    @Override
    public AuthResponse findUserId(FindIdRequest req) {
        return userRepository.findAll().stream()
                .filter(u -> req.getName().equals(u.getName())
                        && req.getEmail().equals(u.getEmail()))
                .findFirst()
                .map(u -> new AuthResponse(true, "아이디 조회 성공", u.getUserId()))
                .orElse(new AuthResponse(false, "일치하는 사용자가 없습니다."));
    }

    @Override
    public AuthResponse resetPassword(ResetPasswordRequest req) {

        return userRepository.findByUserId(req.getUserId())
                .filter(u -> req.getEmail().equals(u.getEmail()))
                .map(u -> {
                    String tempPw = "pw" + (int)(Math.random() * 9000 + 1000);
                    u.setPassword(passwordEncoder.encode(tempPw));
                    u.setUpdated_at(LocalDateTime.now());
                    userRepository.save(u);

                    emailService.sendTemporaryPassword(req.getEmail(), tempPw);

                    return new AuthResponse(true, "임시 비밀번호가 발급되었습니다.", tempPw);
                })
                .orElse(new AuthResponse(false, "일치하는 정보가 없습니다."));
    }

    @Override
    public AuthResponse getProfile(String userId) {

        return userRepository.findByUserId(userId)
                .map(u -> new AuthResponse(true, "조회 성공", u))
                .orElse(new AuthResponse(false, "사용자를 찾을 수 없습니다."));
    }

    @Override
    public AuthResponse update(UpdateRequest req) {

        Optional<Users> opt = userRepository.findByUserId(req.getUserId());
        if (!opt.isPresent()) {
            return new AuthResponse(false, "사용자를 찾을 수 없습니다.");
        }

        Users u = opt.get();

        u.setName(req.getName());
        u.setPhone(req.getPhone());
        u.setEmail(req.getEmail());
        u.setUpdated_at(LocalDateTime.now());
        userRepository.save(u);

        return new AuthResponse(true, "수정 완료", req.getUserId());
    }

    @Override
    public AuthResponse refresh(RefreshRequest req) {

        String refreshToken = req.getRefreshToken();

        if (jwtUtil.isExpired(refreshToken)) {
            return new AuthResponse(false, "Refresh Token 만료. 다시 로그인해주세요.");
        }

        String userId = jwtUtil.getUserId(refreshToken);
        if (userId == null) {
            return new AuthResponse(false, "유효하지 않은 Refresh Token");
        }

        Users user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            return new AuthResponse(false, "사용자를 찾을 수 없습니다.");
        }

        if (!refreshToken.equals(user.getRefreshToken())) {
            return new AuthResponse(false, "Refresh Token이 일치하지 않습니다.");
        }

        String newAccess = jwtUtil.createAccessToken(userId);
        String newRefresh = jwtUtil.createRefreshToken(userId);

        user.setRefreshToken(newRefresh);
        userRepository.save(user);

        TokenResponse tokenResponse = new TokenResponse(newAccess, newRefresh);
        return new AuthResponse(true, "새로운 토큰이 발급되었습니다.", tokenResponse);
    }

    @Override
    public AuthResponse changePassword(ChangePasswordRequest req) {

        Optional<Users> opt = userRepository.findByUserId(req.getUserId());
        if (!opt.isPresent()) {
            return new AuthResponse(false, "사용자를 찾을 수 없습니다.");
        }

        Users user = opt.get();

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            return new AuthResponse(false, "현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setUpdated_at(LocalDateTime.now());
        userRepository.save(user);

        return new AuthResponse(true, "비밀번호가 성공적으로 변경되었습니다.");
    }
}
