package com.bbey.neez.service.Auth;

public interface PasswordResetService {
    void sendResetCode(String userId, String email);
    void resetPassword(String email, String code, String newPassword);
}
