package com.bbey.neez.service.Auth;

import com.bbey.neez.DTO.auth.*;

public interface AuthService {

    AuthResponse register(RegisterRequest req);
    AuthResponse verifyEmail(String token);
    AuthResponse login(LoginRequest req);

    // 로그아웃: idx 기반
    AuthResponse logoutByIdx(Long idx);

    AuthResponse delete(DeleteRequest req);

    AuthResponse findUserId(FindIdRequest req);
    AuthResponse forgotPassword(ForgotPasswordRequest req);
    AuthResponse resetPassword(PasswordResetConfirmRequest req);

    // 프로필 조회/수정: idx 기반
    AuthResponse getProfileByIdx(Long idx);
    AuthResponse updateByIdx(Long idx, UpdateRequest req);

    // 비밀번호 변경: idx + 요청 정보
    AuthResponse changePasswordByIdx(Long idx, ChangePasswordRequest req);

    AuthResponse refresh(RefreshRequest req);
}

