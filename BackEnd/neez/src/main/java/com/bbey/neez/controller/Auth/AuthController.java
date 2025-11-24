package com.bbey.neez.controller.Auth;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.auth.*;
import com.bbey.neez.security.UserPrincipal;
import com.bbey.neez.service.Auth.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "로그인 · 회원가입 · 이메일 인증 · 비밀번호 재설정 · 토큰 재발급 · 로그아웃 · 비밀번호 변경")
public class AuthController {

    private final AuthService authService;

    private ApiResponseDto<Object> wrap(AuthResponse res) {
        return new ApiResponseDto<>(
                res.isSuccess(),
                res.getMessage(),
                res.getData());
    }

    @Operation(summary = "로그인", description = "ID/비밀번호를 이용해 로그인하고 Access/Refresh 토큰을 반환합니다.")
    @PostMapping("/login")
    public ApiResponseDto<Object> login(@Valid @RequestBody LoginRequest req) {
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

    @Operation(summary = "비밀번호 재설정 메일 요청 ( 비밀번호 잊음 )", description = "등록된 이메일 주소로 비밀번호 재설정 코드(인증코드)를 전송합니다.")
    @PostMapping("/forgot-password")
    public ApiResponseDto<Object> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        return wrap(authService.forgotPassword(req));
    }

    @Operation(summary = "비밀번호 재설정 ( 비밀번호 잊음 )", description = "이메일 + 인증코드 + 새 비밀번호로 비밀번호를 재설정합니다.")
    @PostMapping("/reset-password")
    public ApiResponseDto<Object> resetPassword(@RequestBody PasswordResetConfirmRequest req) {
        return wrap(authService.resetPassword(req));
    }

    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자의 Refresh Token을 삭제하여 로그아웃합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") })
    @PostMapping("/me/logout")
    public ApiResponseDto<Object> logout(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal user) {
        Long idx = user.getIdx();
        AuthResponse res = authService.logoutByIdx(idx);
        return wrap(res);
    }

    @Operation(summary = "비밀번호 변경 ( 로그인된 사용자 )", description = "현재 로그인한 사용자의 비밀번호를 변경합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") })
    @PostMapping("/me/change-password")
    public ApiResponseDto<Object> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal user,
            @RequestBody ChangePasswordRequest req) {
        Long idx = user.getIdx();
        AuthResponse res = authService.changePasswordByIdx(idx, req);
        return wrap(res);
    }

    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 사용해 새로운 Access/Refresh 토큰을 발급합니다.")
    @PostMapping("/refresh")
    public ApiResponseDto<Object> refresh(@RequestBody RefreshRequest req) {
        return wrap(authService.refresh(req));
    }

    @GetMapping("/check-id")
    @Operation(summary = "아이디 중복 체크", description = "쿼리 파라미터로 전달된 userId가 이미 사용 중인지 확인합니다.")
    public ApiResponseDto<Object> checkUserId(
            @Parameter(description = "중복 여부를 확인할 아이디", example = "testuser1")
            @RequestParam(name = "userId", required = false) String userId) {

        if (userId == null || userId.trim().isEmpty()) {
            AuthResponse error = new AuthResponse(false, "userId는 필수입니다.", null);
            return wrap(error);
        }

        boolean exists = authService.isUserIdDuplicate(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("exists", exists);

        String message = exists ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.";

        AuthResponse res = new AuthResponse(true, message, data);
        return wrap(res);
    }

    @GetMapping("/check-email")
    @Operation(summary = "이메일 중복 체크", description = "쿼리 파라미터로 전달된 email이 이미 사용 중인지 확인합니다.")
    public ApiResponseDto<Object> checkEmail(
            @Parameter(description = "중복 여부를 확인할 이메일", example = "testuser1@example.com")
            @RequestParam(name = "email", required = false) String email) {

        if (email == null || email.trim().isEmpty()) {
            AuthResponse error = new AuthResponse(false, "email은 필수입니다.", null);
            return wrap(error);
        }

        boolean exists = authService.isEmailDuplicate(email);

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("exists", exists);

        String message = exists ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.";

        AuthResponse res = new AuthResponse(true, message, data);
        return wrap(res);
    }

}
