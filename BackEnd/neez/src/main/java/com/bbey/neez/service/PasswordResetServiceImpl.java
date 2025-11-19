package com.bbey.neez.service;

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
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 1단계: 아이디 + 이메일로 사용자 확인 후
     * 6자리 인증코드 생성해서 이메일로 전송
     */
    @Override
    public void sendResetCode(String userId, String email) {

        // 아이디 + 이메일로 사용자 존재 여부 확인
        Optional<Users> userOpt = userRepository.findByUserIdAndEmail(userId, email);

        // 보안상 "없다"가 바로 드러나지 않게 그냥 리턴만 함
        if (!userOpt.isPresent()) {
            return;
        }

        // 6자리 인증코드 생성
        String code = String.format("%06d", new Random().nextInt(999999));

        // 기존 토큰 삭제
        passwordResetTokenRepository.deleteByEmail(email);

        // 새 토큰 저장
        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setCode(code);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(10));

        passwordResetTokenRepository.save(token);

        // 이메일 발송 (Reset 코드용 메일)
        emailService.sendResetCodeEmail(email, code);
    }

    /**
     * 2단계: 이메일 + 코드로 토큰 검증 후
     * 새 비밀번호로 변경
     */
    @Override
    public void resetPassword(String email, String code, String newPassword) {

        // 이메일 + 코드로 토큰 조회
        Optional<PasswordResetToken> tokenOpt =
                passwordResetTokenRepository.findByEmailAndCode(email, code);

        if (!tokenOpt.isPresent()) {
            // 잘못된 코드 → 그냥 종료 (에러 응답을 따로 주고 싶으면 예외 던지고 @ControllerAdvice 로 처리)
            return;
        }

        PasswordResetToken token = tokenOpt.get();

        // 만료 여부 체크
        if (token.isExpired()) {
            return;
        }

        // 이메일로 사용자 조회
        Optional<Users> userOpt = userRepository.findByEmail(email);

        if (!userOpt.isPresent()) {
            return;
        }

        Users user = userOpt.get();

        // 새 비밀번호 암호화 저장
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdated_at(LocalDateTime.now());
        userRepository.save(user);

        // 토큰 제거
        passwordResetTokenRepository.delete(token);
    }
}
