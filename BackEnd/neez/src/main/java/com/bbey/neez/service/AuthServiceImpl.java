package com.bbey.neez.service;

import com.bbey.neez.DTO.auth.*;
import com.bbey.neez.entity.EmailVerificationToken;
import com.bbey.neez.entity.Users;
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

    // ========================
    // 📌 회원가입 (이메일 인증 포함)
    // ========================
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

        // 이메일 인증 토큰 생성
        String token = UUID.randomUUID().toString();

        EmailVerificationToken emailToken = new EmailVerificationToken();
        emailToken.setToken(token);
        emailToken.setUser(u);
        emailToken.setExpiryDate(LocalDateTime.now().plusHours(24));

        tokenRepository.save(emailToken);

        // 이메일 전송
        emailService.sendVerificationEmail(u, token);

        return new AuthResponse(true, "회원가입 성공! 이메일 인증을 완료해주세요.", req.getUserId());
    }

    // ========================
    // 📌 이메일 인증
    // ========================
    @Override
    public AuthResponse verifyEmail(String token) {

        EmailVerificationToken emailToken = tokenRepository.findByToken(token)
                .orElse(null);

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

    // ========================
    // 📌 로그인
    // ========================
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

        return new AuthResponse(true, "로그인 성공", user.getName());
    }

    // ========================
    // 📌 로그아웃
    // ========================
    @Override
    public AuthResponse logout(LogoutRequest req) {
        return new AuthResponse(true, "로그아웃 완료", req.getUserId());
    }

    // ========================
    // 📌 회원탈퇴
    // ========================
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

    // ========================
    // 📌 아이디 찾기
    // ========================
    @Override
    public AuthResponse findUserId(FindIdRequest req) {

        return userRepository.findAll().stream()
                .filter(u -> req.getName().equals(u.getName()) &&
                             req.getEmail().equals(u.getEmail()))
                .findFirst()
                .map(u -> new AuthResponse(true, "아이디 조회 성공", u.getUserId()))
                .orElse(new AuthResponse(false, "일치하는 사용자가 없습니다."));
    }

    // ========================
    // 📌 비밀번호 재설정 (임시발급)
    // ========================
    @Override
    public AuthResponse resetPassword(ResetPasswordRequest req) {

        return userRepository.findByUserId(req.getUserId())
                .filter(u -> req.getEmail().equals(u.getEmail()))
                .map(u -> {

                    String tempPw = "pw" + (int) (Math.random() * 9000 + 1000);

                    u.setPassword(passwordEncoder.encode(tempPw));
                    u.setUpdated_at(LocalDateTime.now());
                    userRepository.save(u);

                    emailService.sendTemporaryPassword(req.getEmail(), tempPw);

                    return new AuthResponse(true, "임시 비밀번호가 발급되었습니다.", tempPw);
                })
                .orElse(new AuthResponse(false, "일치하는 정보가 없습니다."));
    }

    // ========================
    // 📌 회원정보 조회
    // ========================
    @Override
    public AuthResponse getProfile(String userId) {

        return userRepository.findByUserId(userId)
                .map(u -> new AuthResponse(true, "조회 성공", u))
                .orElse(new AuthResponse(false, "사용자를 찾을 수 없습니다."));
    }

    // ========================
    // 📌 회원정보 수정
    // ========================
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
}
