package com.bbey.neez.controller;

import com.bbey.neez.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ✅ 회원가입
    @PostMapping("/register")
    public String register(@RequestParam String userId,
                           @RequestParam String password,
                           @RequestParam String name,
                           @RequestParam String email) {
        return authService.register(userId, password, name, email);
    }

    // ✅ 로그인
    @PostMapping("/login")
    public String login(@RequestParam String userId,
                        @RequestParam String password) {
        return authService.login(userId, password);
    }

    // ✅ 로그아웃
    @PostMapping("/logout")
    public String logout(@RequestParam String userId) {
        return authService.logout(userId);
    }

    // ✅ 회원탈퇴
    @DeleteMapping("/delete")
    public String delete(@RequestParam String userId,
                         @RequestParam String password) {
        return authService.delete(userId, password);
    }

    // ✅ 아이디 찾기
    @GetMapping("/find-id")
    public String findId(@RequestParam String name, @RequestParam String email) {
        return authService.findUserId(name, email);
    }

    // ✅ 비밀번호 찾기
    @PostMapping("/find-password")
    public String findPassword(@RequestParam String userId, @RequestParam String email) {
        return authService.resetPassword(userId, email);
    }
}
