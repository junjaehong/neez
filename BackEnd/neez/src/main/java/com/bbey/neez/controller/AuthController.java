package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.auth.*;
import com.bbey.neez.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Auth API", description = "회원 인증 관련 API")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    public ApiResponseDto<AuthResponse> register(@RequestBody RegisterRequest req) {
        AuthResponse res = authService.register(req);
        return new ApiResponseDto<>(res.isSuccess(), res.getMessage(), res);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponseDto<AuthResponse> login(@RequestBody LoginRequest req) {
        AuthResponse res = authService.login(req);
        return new ApiResponseDto<>(res.isSuccess(), res.getMessage(), res);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponseDto<AuthResponse> logout(@RequestBody LogoutRequest req) {
        AuthResponse res = authService.logout(req);
        return new ApiResponseDto<>(res.isSuccess(), res.getMessage(), res);
    }

    @Operation(summary = "회원탈퇴")
    @DeleteMapping("/delete")
    public ApiResponseDto<AuthResponse> delete(@RequestBody DeleteRequest req) {
        AuthResponse res = authService.delete(req);
        return new ApiResponseDto<>(res.isSuccess(), res.getMessage(), res);
    }

    @Operation(summary = "아이디 찾기")
    @PostMapping("/find-id")
    public ApiResponseDto<AuthResponse> findId(@RequestBody FindIdRequest req) {
        AuthResponse res = authService.findUserId(req);
        return new ApiResponseDto<>(res.isSuccess(), res.getMessage(), res);
    }

    @Operation(summary = "비밀번호 찾기")
    @PostMapping("/find-password")
    public ApiResponseDto<AuthResponse> findPassword(@RequestBody ResetPasswordRequest req) {
        AuthResponse res = authService.resetPassword(req);
        return new ApiResponseDto<>(res.isSuccess(), res.getMessage(), res);
    }
}
