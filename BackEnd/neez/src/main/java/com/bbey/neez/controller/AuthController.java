package com.bbey.neez.controller;

import com.bbey.neez.dto.auth.*;
import com.bbey.neez.dto.ApiResponseDto;
import com.bbey.neez.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 공통 응답 래퍼
    private <T> ApiResponseDto<T> wrap(T data) {
        if (data instanceof AuthResponse) {
            AuthResponse ar = (AuthResponse) data;
            return new ApiResponseDto<>(ar.isSuccess(), ar.getMessage(), (T) ar.getData());
        }
        return new ApiResponseDto<>(true, "요청 성공", data);
    }

    @PostMapping("/login")
    public ApiResponseDto<AuthResponse> login(@RequestBody LoginRequest req) {
        return wrap(authService.login(req));
    }

    @PostMapping("/register")
    public ApiResponseDto<AuthResponse> register(@RequestBody RegisterRequest req) {
        return wrap(authService.register(req));
    }

    @GetMapping("/verify")
    public ApiResponseDto<AuthResponse> verify(@RequestParam String token) {
        return wrap(authService.verifyEmail(token));
    }

    @PostMapping("/logout")
    public ApiResponseDto<AuthResponse> logout(@RequestBody LogoutRequest req) {
        return wrap(authService.logout(req));
    }

    @PostMapping("/delete")
    public ApiResponseDto<AuthResponse> delete(@RequestBody DeleteRequest req) {
        return wrap(authService.delete(req));
    }

    @PostMapping("/find-id")
    public ApiResponseDto<AuthResponse> findId(@RequestBody FindIdRequest req) {
        return wrap(authService.findUserId(req));
    }

    @PostMapping("/reset-password") 
    public ApiResponseDto<AuthResponse> reset(@RequestBody ResetPasswordRequest req) {
        return wrap(authService.resetPassword(req));
    }

    @GetMapping("/profile/{userId}")
    public ApiResponseDto<AuthResponse> getProfile(@PathVariable String userId) {
        return wrap(authService.getProfile(userId));
    }

    @PostMapping("/update")
    public ApiResponseDto<AuthResponse> update(@RequestBody UpdateRequest req) {
        return wrap(authService.update(req));
    }

    @PostMapping("/change-password")
    public ApiResponseDto<AuthResponse> changePassword(@RequestBody ChangePasswordRequest req) {
        return wrap(authService.changePassword(req));
    }

    @PostMapping("/refresh")
    public ApiResponseDto<AuthResponse> refresh(@RequestBody RefreshRequest req) {
        return wrap(authService.refresh(req));
    }

}
