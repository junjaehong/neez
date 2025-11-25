package com.bbey.neez.service.Auth;

import com.bbey.neez.entity.Users;
import com.bbey.neez.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // STEP 1: 인증코드 발송
    @Override
    public void sendResetCode(String userId, String email) {

        Users user = userRepository.findByUserIdAndEmail(userId, email)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 이메일이 잘못되었습니다."));

        // 6자리 랜덤 코드 생성
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);

        // DB에 저장
        user.setResetCode(code);
        user.setResetCodeExpire(LocalDateTime.now().plusMinutes(5)); // 5분 유효
        userRepository.save(user);

        // 이메일 발송
        emailService.sendResetCodeEmail(email, code);
    }

    // STEP 2: 인증코드 검증 후 비밀번호 변경
    @Override
    public void resetPassword(String email, String code, String newPassword) {

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 1) 코드 존재 확인
        if (user.getResetCode() == null || !user.getResetCode().equals(code)) {
            throw new IllegalArgumentException("잘못된 인증코드입니다.");
        }

        // 2) 만료 확인
        if (user.getResetCodeExpire() == null ||
                user.getResetCodeExpire().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증코드가 만료되었습니다.");
        }

        // 3) 비밀번호 변경
        user.setPassword(passwordEncoder.encode(newPassword));

        // 4) 코드 초기화 (보안상 중요)
        user.setResetCode(null);
        user.setResetCodeExpire(null);

        userRepository.save(user);
    }
}
