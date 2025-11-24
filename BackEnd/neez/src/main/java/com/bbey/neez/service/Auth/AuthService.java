package com.bbey.neez.service.Auth;

import com.bbey.neez.DTO.auth.*;

public interface AuthService {

    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
    AuthResponse logoutByIdx(Long idx);
    AuthResponse delete(DeleteRequest req);
    AuthResponse findUserId(FindIdRequest req);
    AuthResponse forgotPassword(ForgotPasswordRequest req);
    AuthResponse resetPassword(PasswordResetConfirmRequest req);
    AuthResponse verifyEmail(String token);
    AuthResponse getProfileByIdx(Long idx);
    AuthResponse updateByIdx(Long idx, UpdateRequest req);
    AuthResponse changePasswordByIdx(Long idx, ChangePasswordRequest req);
    AuthResponse refresh(RefreshRequest req);

    // ===============================
    // 추가: 중복 체크
    // ===============================
    boolean isUserIdDuplicate(String userId);

    boolean isEmailDuplicate(String email);
}


