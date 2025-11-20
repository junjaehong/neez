package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.auth.AuthResponse;
import com.bbey.neez.DTO.auth.UpdateRequest;
import com.bbey.neez.service.AuthService;
import com.bbey.neez.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "로그인한 회원의 프로필 조회 및 수정 API")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final AuthService authService;

    private ApiResponseDto<Object> wrap(AuthResponse res) {
        return new ApiResponseDto<>(
                res.isSuccess(),
                res.getMessage(),
                res.getData());
    }

    // ✅ 내 프로필 조회 (idx 안 받음)
    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필을 조회합니다.")
    @GetMapping("/me")
    public ApiResponseDto<Object> getMyProfile(
            @Parameter(hidden = true) // Swagger에 표시되지 않도록 숨김
            @AuthenticationPrincipal UserPrincipal user) {
        Long idx = user.getIdx(); // <-- 너네 UserPrincipal에 맞게 getIdx / getId 등으로 수정
        AuthResponse res = authService.getProfileByIdx(idx);
        return wrap(res);
    }

    // ✅ 내 정보 수정
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @PostMapping("/me")
    public ApiResponseDto<Object> updateMyProfile(
            @Parameter(hidden = true) // Swagger에 표시되지 않도록 숨김
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody UpdateRequest req) {
        Long idx = user.getIdx(); // 토큰 기준
        AuthResponse res = authService.updateByIdx(idx, req);
        return wrap(res);
    }
}
