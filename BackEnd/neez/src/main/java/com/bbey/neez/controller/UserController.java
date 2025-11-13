package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.auth.AuthResponse;
import com.bbey.neez.DTO.auth.UpdateRequest;
import com.bbey.neez.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "User API", description = "회원 정보 조회 및 수정 API")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원 정보 조회")
    @GetMapping("/{userId}")
    public ApiResponseDto<AuthResponse> getProfile(@PathVariable String userId) {
        AuthResponse res = authService.getProfile(userId);
        return new ApiResponseDto<>(res.isSuccess(), res.getMessage(), res);
    }

    @Operation(summary = "회원 정보 수정")
    @PutMapping("/update")
    public ApiResponseDto<AuthResponse> update(@RequestBody UpdateRequest req) {
        AuthResponse res = authService.update(req);
        return new ApiResponseDto<>(res.isSuccess(), res.getMessage(), res);
    }
}
