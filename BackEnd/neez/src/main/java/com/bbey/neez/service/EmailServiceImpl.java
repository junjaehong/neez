package com.bbey.neez.service;

import com.bbey.neez.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(Users user, String token) {
        String link = "http://localhost:8083/api/auth/verify?token=" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(user.getEmail());
        msg.setSubject("Neez 이메일 인증");
        msg.setText("아래 링크를 눌러 이메일 인증을 완료해주세요.\n\n" + link);

        mailSender.send(msg);
    }

    @Override
    public void sendTemporaryPassword(String email, String tempPassword) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Neez 임시 비밀번호 안내");
        msg.setText("임시 비밀번호: " + tempPassword + "\n로그인 후 반드시 비밀번호를 변경해주세요.");

        mailSender.send(msg);
    }

    @Override
    public void sendResetCodeEmail(String email, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Neez 비밀번호 재설정 인증코드");
        msg.setText(
                "비밀번호 재설정을 위한 인증코드는 다음과 같습니다.\n\n" +
                "인증코드: " + code + "\n\n" +
                "⚠ 10분 이내에 입력해주세요."
        );

        mailSender.send(msg);
    }
}
