package com.bbey.neez.controller;

import com.bbey.neez.dto.auth.ForgotPasswordRequest;
import com.bbey.neez.dto.auth.PasswordResetConfirmRequest;
import com.bbey.neez.service.PasswordResetService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetCode(request.getUserId(), request.getEmail());
        return ResponseEntity.ok("인증코드가 이메일로 전송되었습니다.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.resetPassword(
                request.getEmail(),
                request.getCode(),
                request.getNewPassword()
        );
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}
