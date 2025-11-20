package com.bbey.neez.service;

import com.bbey.neez.DTO.auth.*;
import com.bbey.neez.entity.Users;
import com.bbey.neez.jwt.JwtUtil;
import com.bbey.neez.repository.UserRepository;
import com.bbey.neez.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final PasswordResetService passwordResetService;

    // ===============================
    //  회원가입
    // ===============================
    @Override
    public AuthResponse register(RegisterRequest req) {

        if (userRepository.findByUserId(req.getUserId()).isPresent()) {
            return new AuthResponse(false, "이미 사용 중인 아이디입니다.");
        }

        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return new AuthResponse(false, "이미 사용 중인 이메일입니다.");
        }

        Users user = new Users();
        user.setUserId(req.getUserId());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setVerified(false);
        user.setCreated_at(LocalDateTime.now());

        userRepository.save(user);

        emailService.sendVerificationEmail(user, user.getUserId());

        return new AuthResponse(true, "회원가입 성공! 이메일 인증을 완료해주세요.");
    }


    // ===============================
    //  로그인 (정석 방식)
    // ===============================
    @Override
    public AuthResponse login(LoginRequest req) {

        // Spring Security가 비밀번호 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getUserId(),
                        req.getPassword()
                )
        );

        // 인증 성공 → UserPrincipal 반환됨
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Users user = principal.getUser();

        if (!user.isVerified()) {
            return new AuthResponse(false, "이메일 인증을 완료해주세요.");
        }

        // JWT 생성
        String access = jwtUtil.createAccessToken(user.getUserId());
        String refresh = jwtUtil.createRefreshToken(user.getUserId());

        user.setRefreshToken(refresh);
        userRepository.save(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", access);
        tokens.put("refreshToken", refresh);

        return new AuthResponse(true, "로그인 성공", tokens);
    }


    // ===============================
    //  로그아웃
    // ===============================
    @Override
    public AuthResponse logout(LogoutRequest req) {

        Optional<Users> userOpt = userRepository.findByUserId(req.getUserId());
        if (!userOpt.isPresent()) {   // Java 8 호환
            return new AuthResponse(false, "존재하지 않는 유저입니다.");
        }

        Users user = userOpt.get();
        user.setRefreshToken(null);
        userRepository.save(user);

        return new AuthResponse(true, "로그아웃 완료");
    }


    // ===============================
    //  회원 탈퇴
    // ===============================
    @Override
    public AuthResponse delete(DeleteRequest req) {

        Optional<Users> userOpt = userRepository.findByUserId(req.getUserId());
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 유저입니다.");
        }

        Users user = userOpt.get();

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return new AuthResponse(false, "비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);

        return new AuthResponse(true, "회원 탈퇴 완료");
    }


    // ===============================
    //  아이디 찾기
    // ===============================
    @Override
    public AuthResponse findUserId(FindIdRequest req) {

        Optional<Users> userOpt = userRepository.findByNameAndEmail(req.getName(), req.getEmail());
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "일치하는 회원 정보를 찾을 수 없습니다.");
        }

        return new AuthResponse(true, "아이디 찾기 성공", userOpt.get().getUserId());
    }


    // ===============================
    //  비밀번호 재설정 (1단계)
    // ===============================
    @Override
    public AuthResponse forgotPassword(ForgotPasswordRequest req) {

        passwordResetService.sendResetCode(req.getUserId(), req.getEmail());
        return new AuthResponse(true, "인증코드를 이메일로 전송했습니다.");
    }


    // ===============================
    //  비밀번호 재설정 (2단계)
    // ===============================
    @Override
    public AuthResponse resetPassword(PasswordResetConfirmRequest req) {

        passwordResetService.resetPassword(req.getEmail(), req.getCode(), req.getNewPassword());
        return new AuthResponse(true, "비밀번호가 변경되었습니다.");
    }


    // ===============================
    //  이메일 인증
    // ===============================
    @Override
    public AuthResponse verifyEmail(String token) {

        Optional<Users> userOpt = userRepository.findByUserId(token);
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "잘못된 인증 요청입니다.");
        }

        Users user = userOpt.get();
        user.setVerified(true);
        userRepository.save(user);

        return new AuthResponse(true, "이메일 인증 완료");
    }


    // ===============================
    //  프로필 조회
    // ===============================
    @Override
    public AuthResponse getProfile(String userId) {

        Optional<Users> userOpt = userRepository.findByUserId(userId);
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 유저입니다.");
        }

        return new AuthResponse(true, "조회 성공", userOpt.get());
    }


    // ===============================
    //  프로필 수정
    // ===============================
    @Override
    public AuthResponse update(UpdateRequest req) {

        Optional<Users> userOpt = userRepository.findByUserId(req.getUserId());
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 유저입니다.");
        }

        Users user = userOpt.get();

        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setUpdated_at(LocalDateTime.now());

        userRepository.save(user);

        return new AuthResponse(true, "정보 수정 완료", user);
    }


    // ===============================
    //  비밀번호 변경
    // ===============================
    @Override
    public AuthResponse changePassword(ChangePasswordRequest req) {

        Optional<Users> userOpt = userRepository.findByUserId(req.getUserId());
        if (!userOpt.isPresent()) {   // Java 8 호환
            return new AuthResponse(false, "존재하지 않는 유저입니다.");
        }

        Users user = userOpt.get();

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            return new AuthResponse(false, "현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return new AuthResponse(true, "비밀번호 변경 성공");
    }


    // ===============================
    //  Refresh Token 재발급
    // ===============================
    @Override
    public AuthResponse refresh(RefreshRequest req) {

        String refreshToken = req.getRefreshToken();

        if (refreshToken == null) {
            return new AuthResponse(false, "Refresh Token이 필요합니다.");
        }

        String userId = jwtUtil.getUserIdFromToken(refreshToken);

        if (userId == null || jwtUtil.isExpired(refreshToken)) {
            return new AuthResponse(false, "Refresh Token이 유효하지 않습니다.");
        }

        Optional<Users> userOpt = userRepository.findByUserId(userId);
        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 유저입니다.");
        }

        Users user = userOpt.get();

        if (user.getRefreshToken() == null ||
            !user.getRefreshToken().equals(refreshToken)) {

            return new AuthResponse(false, "Refresh Token이 서버 정보와 일치하지 않습니다.");
        }

        // 새 토큰 발급
        String newAccess = jwtUtil.createAccessToken(userId);
        String newRefresh = jwtUtil.createRefreshToken(userId);

        user.setRefreshToken(newRefresh);
        userRepository.save(user);

        Map<String, String> map = new HashMap<>();
        map.put("accessToken", newAccess);
        map.put("refreshToken", newRefresh);

        return new AuthResponse(true, "토큰 재발급 성공", map);
    }
}
