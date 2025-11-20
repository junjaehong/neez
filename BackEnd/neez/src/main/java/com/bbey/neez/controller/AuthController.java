package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.auth.*;
import com.bbey.neez.security.UserPrincipal;
import com.bbey.neez.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 공통 응답 래핑
    private ApiResponseDto<Object> wrap(AuthResponse res) {
        return new ApiResponseDto<>(
                res.isSuccess(),
                res.getMessage(),
                res.getData()
        );
    }

    // JWT에서 현재 유저의 idx 꺼내기
    private Long getCurrentUserIdx() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        return principal.getUser().getIdx();
    }

    // ================= 로그인 / 회원가입 / 이메일 인증 =================

    // 로그인
    @PostMapping("/login")
    public ApiResponseDto<Object> login(@RequestBody LoginRequest req) {
        return wrap(authService.login(req));
    }

    // 회원가입 (A안: Users에 바로 INSERT 안 하고 토큰 테이블에만 저장)
    @PostMapping("/register")
    public ApiResponseDto<Object> register(@RequestBody RegisterRequest req) {
        return wrap(authService.register(req));
    }

    // 이메일 인증 (토큰으로 Users INSERT)
    @GetMapping("/verify")
    public ApiResponseDto<Object> verify(@RequestParam("token") String token) {
        return wrap(authService.verifyEmail(token));
    }

    // ================= 로그아웃 / 탈퇴 =================

    // 로그아웃: userId 안 받고, 현재 토큰의 idx 기준
    @PostMapping("/logout")
    public ApiResponseDto<Object> logout() {
        Long idx = getCurrentUserIdx();
        return wrap(authService.logoutByIdx(idx));
    }

    // 회원탈퇴: 현재 구조는 DeleteRequest(userId, password) 기준으로 유지
    @PostMapping("/delete")
    public ApiResponseDto<Object> delete(@RequestBody DeleteRequest req) {
        return wrap(authService.delete(req));
    }

    // ================= 아이디 찾기 / 비밀번호 재설정 (비로그인) =================

    // 아이디 찾기
    @PostMapping("/find-id")
    public ApiResponseDto<Object> findId(@RequestBody FindIdRequest req) {
        return wrap(authService.findUserId(req));
    }

    // 비밀번호 찾기 1단계: 인증코드 메일 전송
    @PostMapping("/forgot-password")
    public ApiResponseDto<Object> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        return wrap(authService.forgotPassword(req));
    }

    // 비밀번호 재설정 2단계: 코드 검증 후 비번 변경
    @PostMapping("/reset-password")
    public ApiResponseDto<Object> resetPassword(@RequestBody PasswordResetConfirmRequest req) {
        return wrap(authService.resetPassword(req));
    }

    // ================= 프로필 조회 / 수정 (현재 로그인 유저 기준) =================

    // 내 프로필 조회 (URL에 userId / idx 노출 X)
    @GetMapping("/profile")
    public ApiResponseDto<Object> getProfile() {
        Long idx = getCurrentUserIdx();
        return wrap(authService.getProfileByIdx(idx));
    }

    // 내 프로필 수정 (idx는 토큰에서 가져옴, email 변경은 금지 정책)
    @PostMapping("/update")
    public ApiResponseDto<Object> update(@RequestBody UpdateRequest req) {
        Long idx = getCurrentUserIdx();
        return wrap(authService.updateByIdx(idx, req));
    }

    // ================= 비밀번호 변경 (로그인 상태) =================

    @PostMapping("/change-password")
    public ApiResponseDto<Object> changePassword(@RequestBody ChangePasswordRequest req) {
        Long idx = getCurrentUserIdx();
        return wrap(authService.changePasswordByIdx(idx, req));
    }

    // ================= 토큰 재발급 =================

    @PostMapping("/refresh")
    public ApiResponseDto<Object> refresh(@RequestBody RefreshRequest req) {
        return wrap(authService.refresh(req));
    }
}
