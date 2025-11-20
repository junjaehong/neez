package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.auth.*;
import com.bbey.neez.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "로그인 · 회원가입 · 이메일 인증 · 비밀번호 재설정 · 토큰 재발급")
public class AuthController {

    private final AuthService authService;

    private ApiResponseDto<Object> wrap(AuthResponse res) {
        return new ApiResponseDto<>(
                res.isSuccess(),
                res.getMessage(),
                res.getData()
        );
    }

    @Operation(summary = "로그인", description = "ID/비밀번호를 이용해 로그인하고 Access/Refresh 토큰을 반환합니다.")
    @PostMapping("/login")
    public ApiResponseDto<Object> login(@RequestBody LoginRequest req) {
        return wrap(authService.login(req));
    }

    @Operation(summary = "회원가입", description = "회원가입을 요청하면 이메일 인증 메일이 전송됩니다.")
    @PostMapping("/register")
    public ApiResponseDto<Object> register(@RequestBody RegisterRequest req) {
        return wrap(authService.register(req));
    }

    @Operation(summary = "이메일 인증", description = "메일로 전송된 인증 링크 클릭 시 회원가입이 완료됩니다.")
    @GetMapping("/verify")
    public ApiResponseDto<Object> verify(@RequestParam("token") String token) {
        return wrap(authService.verifyEmail(token));
    }

    @Operation(summary = "비밀번호 재설정 메일 요청", description = "등록된 이메일 주소로 비밀번호 재설정 코드(인증코드)를 전송합니다.")
    @PostMapping("/forgot-password")
    public ApiResponseDto<Object> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        return wrap(authService.forgotPassword(req));
    }

    @Operation(summary = "비밀번호 재설정", description = "이메일 + 인증코드 + 새 비밀번호로 비밀번호를 재설정합니다.")
    @PostMapping("/reset-password")
    public ApiResponseDto<Object> resetPassword(@RequestBody PasswordResetConfirmRequest req) {
        return wrap(authService.resetPassword(req));
    }

    @Operation(
            summary = "비밀번호 변경",
            description = "로그인된 사용자의 비밀번호를 변경합니다. (PK idx 기준)",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @PostMapping("/change-password/{idx}")
    public ApiResponseDto<Object> changePassword(
            @PathVariable Long idx,
            @RequestBody ChangePasswordRequest req
    ) {
        AuthResponse res = authService.changePasswordByIdx(idx, req);
        return wrap(res);
    }

    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 사용해 새로운 Access/Refresh 토큰을 발급합니다.")
    @PostMapping("/refresh")
    public ApiResponseDto<Object> refresh(@RequestBody RefreshRequest req) {
        return wrap(authService.refresh(req));
    }
}
