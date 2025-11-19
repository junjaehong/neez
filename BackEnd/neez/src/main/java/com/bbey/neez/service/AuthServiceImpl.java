package com.bbey.neez.service;

import com.bbey.neez.DTO.auth.*;
import com.bbey.neez.entity.PasswordResetToken;
import com.bbey.neez.entity.Users;
import com.bbey.neez.repository.PasswordResetTokenRepository;
import com.bbey.neez.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // ===========================
    // 1) 회원가입
    // ===========================
    @Override
    public AuthResponse register(RegisterRequest req) {

        if (userRepository.findByUserId(req.getUserId()).isPresent()) {
            return new AuthResponse(false, "이미 존재하는 아이디입니다.", null);
        }

        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return new AuthResponse(false, "이미 존재하는 이메일입니다.", null);
        }

        Users user = new Users();
        user.setUserId(req.getUserId());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setVerified(false);
        user.setCreated_at(LocalDateTime.now());
        user.setUpdated_at(LocalDateTime.now());

        userRepository.save(user);

        return new AuthResponse(true, "회원가입 완료", null);
    }

    // ===========================
    // 2) 로그인
    // ===========================
    @Override
    public AuthResponse login(LoginRequest req) {

        Optional<Users> userOpt = userRepository.findByUserId(req.getUserId());

        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "아이디 혹은 비밀번호가 올바르지 않습니다.", null);
        }

        Users user = userOpt.get();

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return new AuthResponse(false, "아이디 혹은 비밀번호가 올바르지 않습니다.", null);
        }

        return new AuthResponse(true, "로그인 성공", user);
    }

    // ===========================
    // 3) 로그아웃
    // ===========================
    @Override
    public AuthResponse logout(LogoutRequest req) {
        return new AuthResponse(true, "로그아웃 완료", null);
    }

    // ===========================
    // 4) 회원탈퇴
    // ===========================
    @Override
    public AuthResponse delete(DeleteRequest req) {

        Optional<Users> userOpt = userRepository.findByUserId(req.getUserId());

        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 사용자입니다.", null);
        }

        userRepository.delete(userOpt.get());
        return new AuthResponse(true, "회원 삭제 완료", null);
    }

    // ===========================
    // 5) 아이디 찾기
    // ===========================
    @Override
    public AuthResponse findUserId(FindIdRequest req) {

        Optional<Users> userOpt = userRepository.findByNameAndEmail(req.getName(), req.getEmail());

        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "일치하는 정보가 없습니다.", null);
        }

        Users user = userOpt.get();
        return new AuthResponse(true, "아이디 찾기 성공", user.getUserId());
    }

    // ===========================
    // 6) 비밀번호 찾기 (1단계: 인증코드 발송)
    // ===========================
    @Override
    public AuthResponse forgotPassword(ForgotPasswordRequest req) {

        Optional<Users> userOpt = userRepository.findByUserIdAndEmail(req.getUserId(), req.getEmail());

        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "일치하는 사용자 정보가 없습니다.", null);
        }

        // 6자리 인증코드 생성
        String code = String.format("%06d", new Random().nextInt(999999));

        // 기존 토큰 삭제
        passwordResetTokenRepository.deleteByEmail(req.getEmail());

        // 새 토큰 저장
        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(req.getEmail());
        token.setCode(code);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(10));

        passwordResetTokenRepository.save(token);

        // 이메일 발송
        emailService.sendTemporaryPassword(req.getEmail(), code);

        return new AuthResponse(true, "인증코드를 이메일로 전송했습니다.", null);
    }

    // ===========================
    // 7) 비밀번호 재설정 (2단계)
    // ===========================
    @Override
    public AuthResponse resetPassword(PasswordResetConfirmRequest req) {

        Optional<PasswordResetToken> tokenOpt =
                passwordResetTokenRepository.findByEmailAndCode(req.getEmail(), req.getCode());

        if (!tokenOpt.isPresent()) {
            return new AuthResponse(false, "잘못된 인증코드입니다.", null);
        }

        PasswordResetToken token = tokenOpt.get();

        if (token.isExpired()) {
            return new AuthResponse(false, "인증코드가 만료되었습니다.", null);
        }

        Optional<Users> userOpt = userRepository.findByEmail(req.getEmail());

        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "사용자를 찾을 수 없습니다.", null);
        }

        Users user = userOpt.get();

        // 새 비밀번호 암호화하여 저장
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setUpdated_at(LocalDateTime.now());

        userRepository.save(user);

        // 토큰 삭제
        passwordResetTokenRepository.delete(token);

        return new AuthResponse(true, "비밀번호가 성공적으로 변경되었습니다.", null);
    }

    // ===========================
    // 8) 이메일 인증 (기존 기능)
    // ===========================
    @Override
    public AuthResponse verifyEmail(String token) {
        return new AuthResponse(true, "이메일 인증 성공", null);
    }

    // ===========================
    // 9) 프로필 조회
    // ===========================
    @Override
    public AuthResponse getProfile(String userId) {

        Optional<Users> userOpt = userRepository.findByUserId(userId);

        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "사용자를 찾을 수 없습니다.", null);
        }

        return new AuthResponse(true, "프로필 조회 성공", userOpt.get());
    }

    // ===========================
    // 10) 정보 수정
    // ===========================
    @Override
    public AuthResponse update(UpdateRequest req) {

        Optional<Users> userOpt = userRepository.findByUserId(req.getUserId());

        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "사용자를 찾을 수 없습니다.", null);
        }

        Users user = userOpt.get();
        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setUpdated_at(LocalDateTime.now());

        userRepository.save(user);

        return new AuthResponse(true, "정보 수정 완료", null);
    }

    // ===========================
    // 11) 비밀번호 변경 (로그인 상태)
    // ===========================
    @Override
    public AuthResponse changePassword(ChangePasswordRequest req) {

        Optional<Users> userOpt = userRepository.findByUserId(req.getUserId());

        if (!userOpt.isPresent()) {
            return new AuthResponse(false, "사용자를 찾을 수 없습니다.", null);
        }

        Users user = userOpt.get();

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return new AuthResponse(true, "비밀번호 변경 성공", null);
    }

    // ===========================
    // 12) 토큰 재발급
    // ===========================
    @Override
    public AuthResponse refresh(RefreshRequest req) {
        return new AuthResponse(true, "새 토큰 발급됨", null);
    }
}
