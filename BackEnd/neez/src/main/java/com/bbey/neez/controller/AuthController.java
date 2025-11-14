package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.auth.*;
import com.bbey.neez.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "회원 인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    public ApiResponseDto<AuthResponse> register(@RequestBody RegisterRequest req) {
        return wrap(authService.register(req));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponseDto<AuthResponse> login(@RequestBody LoginRequest req) {
        return wrap(authService.login(req));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponseDto<AuthResponse> logout(@RequestBody LogoutRequest req) {
        return wrap(authService.logout(req));
    }

    @Operation(summary = "회원탈퇴")
    @DeleteMapping("/delete")
    public ApiResponseDto<AuthResponse> delete(@RequestBody DeleteRequest req) {
        return wrap(authService.delete(req));
    }

    @Operation(summary = "아이디 찾기")
    @PostMapping("/find-id")
    public ApiResponseDto<AuthResponse> findId(@RequestBody FindIdRequest req) {
        return wrap(authService.findUserId(req));
    }

    @Operation(summary = "비밀번호 찾기(임시 발급)")
    @PostMapping("/find-password")
    public ApiResponseDto<AuthResponse> findPassword(@RequestBody ResetPasswordRequest req) {
        return wrap(authService.resetPassword(req));
    }

    @Operation(summary = "이메일 인증")
    @GetMapping("/verify")
    public ApiResponseDto<AuthResponse> verify(@RequestParam("token") String token) {
        return wrap(authService.verifyEmail(token));
    }

    private ApiResponseDto<AuthResponse> wrap(AuthResponse res) {
        return new ApiResponseDto<>(res.isSuccess(), res.getMessage(), res);
    }
}
