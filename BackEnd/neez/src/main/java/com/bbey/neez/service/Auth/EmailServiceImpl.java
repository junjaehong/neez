package com.bbey.neez.service.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // 로컬에서는 false, 운영에서는 true 로 설정
    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    // 이메일 인증 링크 베이스 URL (프론트/백 URL로 맞추기)
    @Value("${app.verification.base-url:http://localhost:8083}")
    private String baseUrl;

    // ==============================
    // 1) 회원가입 이메일 인증
    // ==============================
    @Override
    public void sendVerificationEmail(String to, String token) {

        String link = baseUrl + "/api/auth/verify?token=" + token;

        if (!mailEnabled) {
            System.out.println("[DEV] 이메일 전송 생략 (회원가입 인증)");
            System.out.println("[DEV] to = " + to);
            System.out.println("[DEV] verification link = " + link);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Neez 회원가입 이메일 인증");
        message.setText(
                "네이즈(Neez) 회원가입을 위해 아래 링크를 클릭하세요.\n\n"
                        + link + "\n\n"
                        + "이 링크는 일정 시간 후 만료됩니다."
        );

        mailSender.send(message);
    }

    // ==============================
    // 2) 비밀번호 재설정 인증 코드 메일
    // ==============================
    @Override
    public void sendResetCodeEmail(String to, String code) {

        if (!mailEnabled) {
            System.out.println("[DEV] 이메일 전송 생략 (비밀번호 재설정)");
            System.out.println("[DEV] to = " + to);
            System.out.println("[DEV] reset code = " + code);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Neez 비밀번호 재설정 인증코드");
        message.setText(
                "다음 인증코드를 입력하여 비밀번호 재설정을 완료하세요.\n\n"
                        + "인증코드: " + code + "\n\n"
                        + "이 코드는 5분 동안만 유효합니다."
        );

        mailSender.send(message);
    }
}
