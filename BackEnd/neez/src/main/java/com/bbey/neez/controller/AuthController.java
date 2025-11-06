package com.bbey.neez.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.*;

// 🎯 AuthController: 로그인, 회원가입, 로그아웃 등을 담당하는 컨트롤러
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")  // 프론트엔드 연결시 CORS 문제 방지
public class AuthController {

    // ✅ 임시로 메모리 안에 회원정보 저장 (DB 연동 전)
    private Map<String, String> userDB = new HashMap<>();   // <userId, password>
    private Set<String> loggedInUsers = new HashSet<>();    // 로그인된 사용자 목록

    // ✅ 회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestParam String userId,
            @RequestParam String password
    ) {
        if (userDB.containsKey(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("⚠️ 이미 존재하는 사용자입니다.");
        }
        userDB.put(userId, password);
        return ResponseEntity.ok("✅ 회원가입 성공: " + userId);
    }

    // ✅ 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam String userId,
            @RequestParam String password
    ) {
        if (!userDB.containsKey(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ 존재하지 않는 사용자입니다.");
        }
        if (!userDB.get(userId).equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ 비밀번호가 일치하지 않습니다.");
        }
        loggedInUsers.add(userId);
        return ResponseEntity.ok("✅ 로그인 성공: " + userId);
    }

    // ✅ 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String userId) {
        if (loggedInUsers.contains(userId)) {
            loggedInUsers.remove(userId);
            return ResponseEntity.ok("👋 로그아웃 완료: " + userId);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("⚠️ 로그인 상태가 아닙니다.");
    }

    // ✅ 회원탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestParam String userId) {
        if (!userDB.containsKey(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ 해당 사용자가 존재하지 않습니다.");
        }
        userDB.remove(userId);
        loggedInUsers.remove(userId);
        return ResponseEntity.ok("🗑️ 회원탈퇴 완료: " + userId);
    }

    // ✅ 전체 사용자 확인 (테스트용)
    @GetMapping("/all")
    public ResponseEntity<Set<String>> getAllUsers() {
        return ResponseEntity.ok(userDB.keySet());
    }
}
