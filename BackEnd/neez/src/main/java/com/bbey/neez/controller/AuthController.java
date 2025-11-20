package com.bbey.neez.controller;

import com.bbey.neez.DTO.auth.*;
import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Auth API", description = "회원 인증/프로필/비밀번호 관련 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 공통 응답 래퍼
     * AuthResponse 를 받아서 ApiResponseDto 로 감싸서 반환
     * data 에는 AuthResponse.getData() 만 들어간다.
     */
    private ApiResponseDto<Object> wrap(AuthResponse res) {
        return new ApiResponseDto<>(
                res.isSuccess(),
                res.getMessage(),
                res.getData()
        );
    }

    // ----------------------------------------------------------------
    // 로그인
    // ----------------------------------------------------------------
    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponseDto<Object> login(@RequestBody LoginRequest req) {
        return wrap(authService.login(req));
    }

    // ----------------------------------------------------------------
    // 회원가입: Users 에 바로 저장 X, 이메일 인증 토큰만 생성 (A안)
    // ----------------------------------------------------------------
    @Operation(summary = "회원가입 (이메일 인증 전까지 Users 미저장)")
    @PostMapping("/register")
    public ApiResponseDto<Object> register(@RequestBody RegisterRequest req) {
        return wrap(authService.register(req));
    }

    // ----------------------------------------------------------------
    // 이메일 인증: 토큰으로 실제 Users INSERT
    // GET /api/auth/verify?token=...
    // ----------------------------------------------------------------
    @Operation(summary = "이메일 인증")
    @GetMapping("/verify")
    public ApiResponseDto<Object> verify(@RequestParam("token") String token) {
        return wrap(authService.verifyEmail(token));
    }

    // ----------------------------------------------------------------
    // 로그아웃
    // ----------------------------------------------------------------
    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponseDto<Object> logout(@RequestBody LogoutRequest req) {
        return wrap(authService.logout(req));
    }

    // ----------------------------------------------------------------
    // 회원 탈퇴
    // ----------------------------------------------------------------
    @Operation(summary = "회원 탈퇴")
    @PostMapping("/delete")
    public ApiResponseDto<Object> delete(@RequestBody DeleteRequest req) {
        return wrap(authService.delete(req));
    }

    // ----------------------------------------------------------------
    // 아이디 찾기
    // ----------------------------------------------------------------
    @Operation(summary = "아이디 찾기")
    @PostMapping("/find-id")
    public ApiResponseDto<Object> findId(@RequestBody FindIdRequest req) {
        return wrap(authService.findUserId(req));
    }

    // ----------------------------------------------------------------
    // 프로필 조회
    // GET /api/auth/profile?userId=jaehong  ← 너가 쓴 형태랑 일치
    // ----------------------------------------------------------------
    @Operation(summary = "프로필 조회")
    @GetMapping("/profile")
    public ApiResponseDto<Object> getProfile(@RequestParam String userId) {
        return wrap(authService.getProfile(userId));
    }

    // ----------------------------------------------------------------
    // 프로필 수정
    // ----------------------------------------------------------------
    @Operation(summary = "프로필 수정")
    @PostMapping("/update")
    public ApiResponseDto<Object> update(@RequestBody UpdateRequest req) {
        return wrap(authService.update(req));
    }

    // ----------------------------------------------------------------
    // 비밀번호 변경
    // ---------------------------------------------------------------->
    @Operation(summary = "비밀번호 변경 (로그인 상태)")
    @PostMapping("/change-password")
    public ApiResponseDto<Object> changePassword(@RequestBody ChangePasswordRequest req) {
        return wrap(authService.changePassword(req));
    }

    // ----------------------------------------------------------------
    // 토큰 재발급 (Refresh Token)
    // ----------------------------------------------------------------
    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    public ApiResponseDto<Object> refresh(@RequestBody RefreshRequest req) {
        return wrap(authService.refresh(req));
    }
}
