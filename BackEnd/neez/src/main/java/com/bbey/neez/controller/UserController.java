package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.auth.AuthResponse;
import com.bbey.neez.DTO.auth.UpdateRequest;
import com.bbey.neez.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    // 공통 응답 래핑
    private ApiResponseDto<Object> wrap(AuthResponse res) {
        return new ApiResponseDto<>(
                res.isSuccess(),
                res.getMessage(),
                res.getData()
        );
    }

    // 회원 프로필 조회 (PK idx 기반)
    @GetMapping("/profile/{idx}")
    public ApiResponseDto<Object> getProfile(@PathVariable Long idx) {
        AuthResponse res = authService.getProfileByIdx(idx);
        return wrap(res);
    }

    // 회원 정보 수정 (idx는 body 안의 req.idx 사용)
    @PostMapping("/update")
    public ApiResponseDto<Object> update(@RequestBody UpdateRequest req) {
        AuthResponse res = authService.updateByIdx(req.getIdx(), req);
        return wrap(res);
    }
}
