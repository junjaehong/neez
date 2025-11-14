package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.auth.AuthResponse;
import com.bbey.neez.DTO.auth.UpdateRequest;
import com.bbey.neez.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/profile/{userId}")
    public ApiResponseDto<AuthResponse> getProfile(@PathVariable String userId) {
        return wrap(authService.getProfile(userId));
    }

    @PostMapping("/update")
    public ApiResponseDto<AuthResponse> update(@RequestBody UpdateRequest req) {
        return wrap(authService.update(req));
    }

    private ApiResponseDto<AuthResponse> wrap(AuthResponse res) {
        return new ApiResponseDto<>(res.isSuccess(), res.getMessage(), res);
    }
}
