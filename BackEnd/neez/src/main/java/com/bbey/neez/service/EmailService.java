package com.bbey.neez.service;

public interface EmailService {

    // 회원가입 이메일 인증
    void sendVerificationEmail(String to, String token);

    // 비밀번호 재설정용 인증코드 메일
    void sendResetCodeEmail(String to, String code);
}
