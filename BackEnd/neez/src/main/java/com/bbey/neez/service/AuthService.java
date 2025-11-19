package com.bbey.neez.service;

import com.bbey.neez.dto.auth.*;

public interface AuthService {

    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
    AuthResponse logout(LogoutRequest req);
    AuthResponse delete(DeleteRequest req);
    AuthResponse findUserId(FindIdRequest req);

    // 수정됨 (resetPassword → forgotPassword + resetPassword)
    AuthResponse forgotPassword(ForgotPasswordRequest req);
    AuthResponse resetPassword(PasswordResetConfirmRequest req);

    AuthResponse verifyEmail(String token);
    AuthResponse getProfile(String userId);
    AuthResponse update(UpdateRequest req);
    AuthResponse changePassword(ChangePasswordRequest req);
    AuthResponse refresh(RefreshRequest req);
}
