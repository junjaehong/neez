package com.bbey.neez.service;

public interface PasswordResetService {
    void sendResetCode(String userId, String email);
    void resetPassword(String email, String code, String newPassword);
}
