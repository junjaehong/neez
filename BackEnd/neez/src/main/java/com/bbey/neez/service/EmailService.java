package com.bbey.neez.service;

import com.bbey.neez.entity.Users;

public interface EmailService {

    void sendVerificationEmail(Users user, String token);

    void sendTemporaryPassword(String email, String tempPassword);

    void sendResetCodeEmail(String email, String code);
}
