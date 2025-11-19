package com.bbey.neez.service;

import com.bbey.neez.dto.auth.*;

public interface AuthService {

    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
    AuthResponse logout(LogoutRequest req);
    AuthResponse delete(DeleteRequest req);
    AuthResponse findUserId(FindIdRequest req);
    AuthResponse resetPassword(ResetPasswordRequest req);

    AuthResponse verifyEmail(String token);

    AuthResponse getProfile(String userId);
    AuthResponse update(UpdateRequest req);

    AuthResponse changePassword(ChangePasswordRequest req);


    // ⭐ Refresh Token으로 새 토큰 발급
    AuthResponse refresh(RefreshRequest req);
}
