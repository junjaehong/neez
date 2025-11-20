package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.auth.AuthResponse;
import com.bbey.neez.DTO.auth.UpdateRequest;
import com.bbey.neez.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "User(Admin) API", description = "관리자 전용 회원 조회 및 수정 API")
public class UserController {

    private final AuthService authService;

    private ApiResponseDto<Object> wrap(AuthResponse res) {
        return new ApiResponseDto<>(
                res.isSuccess(),
                res.getMessage(),
                res.getData()
        );
    }

    @Operation(
            summary = "회원 프로필 조회",
            description = "PK(idx) 기준으로 회원 프로필을 조회합니다.",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @GetMapping("/profile/{idx}")
    public ApiResponseDto<Object> getProfile(@PathVariable Long idx) {
        AuthResponse res = authService.getProfileByIdx(idx);
        return wrap(res);
    }

    @Operation(
            summary = "회원 정보 수정",
            description = "회원 정보를 수정합니다. (idx는 body 내부 req.idx 사용)",
            security = { @SecurityRequirement(name = "BearerAuth") }
    )
    @PostMapping("/update")
    public ApiResponseDto<Object> update(@RequestBody UpdateRequest req) {
        AuthResponse res = authService.updateByIdx(req.getIdx(), req);
        return wrap(res);
    }
}
