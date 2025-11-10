package com.bbey.neez.service;

import com.bbey.neez.DTO.auth.*;

public interface AuthService {

    AuthResponse register(RegisterRequest req);

    AuthResponse login(LoginRequest req);

    AuthResponse logout(LogoutRequest req);

    AuthResponse delete(DeleteRequest req);

    AuthResponse findUserId(FindIdRequest req);

    AuthResponse resetPassword(ResetPasswordRequest req);
}
