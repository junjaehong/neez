package com.bbey.neez.service;

import com.bbey.neez.DTO.auth.*;

public interface AuthService {

    AuthResponse register(RegisterRequest req);

    AuthResponse login(LoginRequest req);

    AuthResponse logout(LogoutRequest req);

    AuthResponse delete(DeleteRequest req);

    AuthResponse findUserId(FindIdRequest req);

    AuthResponse resetPassword(ResetPasswordRequest req);

    AuthResponse verifyEmail(String token);

    AuthResponse getProfile(String userId);

    // ⭐ 추가: 회원 정보 수정
    AuthResponse update(UpdateRequest req);
}
