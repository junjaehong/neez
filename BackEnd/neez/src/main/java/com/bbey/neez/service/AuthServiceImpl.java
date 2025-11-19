package com.bbey.neez.service;

import com.bbey.neez.DTO.auth.*;
import com.bbey.neez.entity.Users;
import com.bbey.neez.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ 회원가입
    @Override
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.findByUserId(req.getUserId()).isPresent()) {
            return new AuthResponse(false, "이미 존재하는 아이디입니다.");
        }

        Users u = new Users();
        u.setUserId(req.getUserId());
        u.setPassword(req.getPassword());
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setCreated_at(LocalDateTime.now());
        u.setUpdated_at(LocalDateTime.now());
        userRepository.save(u);

        return new AuthResponse(true, "회원가입 성공", req.getUserId());
    }

    // ✅ 로그인
    @Override
    public AuthResponse login(LoginRequest req) {
        Optional<Users> optUser = userRepository.findByUserId(req.getUserId());
        if (!optUser.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 아이디입니다.");
        }
        Users user = optUser.get();
        if (!user.getPassword().equals(req.getPassword())) {
            return new AuthResponse(false, "비밀번호가 일치하지 않습니다.");
        }
        return new AuthResponse(true, "로그인 성공", user.getName());
    }

    // ✅ 로그아웃
    @Override
    public AuthResponse logout(LogoutRequest req) {
        // 실제 세션 만료 로직은 별도
        return new AuthResponse(true, "로그아웃 완료", req.getUserId());
    }

    // ✅ 회원탈퇴
    @Override
    public AuthResponse delete(DeleteRequest req) {
        Optional<Users> optUser = userRepository.findByUserId(req.getUserId());
        if (!optUser.isPresent()) {
            return new AuthResponse(false, "존재하지 않는 사용자입니다.");
        }
        Users user = optUser.get();
        if (!user.getPassword().equals(req.getPassword())) {
            return new AuthResponse(false, "비밀번호가 일치하지 않습니다.");
        }
        userRepository.delete(user);
        return new AuthResponse(true, "회원탈퇴 완료", req.getUserId());
    }

    // ✅ 아이디 찾기
    @Override
    public AuthResponse findUserId(FindIdRequest req) {
        return userRepository.findAll().stream()
                .filter(u -> req.getName().equals(u.getName()) && req.getEmail().equals(u.getEmail()))
                .findFirst()
                .map(u -> new AuthResponse(true, "아이디 조회 성공", u.getUserId()))
                .orElse(new AuthResponse(false, "일치하는 사용자가 없습니다."));
    }

    // ✅ 비밀번호 찾기 (임시 비밀번호 생성)
    @Override
    public AuthResponse resetPassword(ResetPasswordRequest req) {
        return userRepository.findByUserId(req.getUserId())
                .filter(u -> req.getEmail().equals(u.getEmail()))
                .map(u -> {
                    String tempPw = "pw" + (int) (Math.random() * 9000 + 1000);
                    u.setPassword(tempPw);
                    u.setUpdated_at(LocalDateTime.now());
                    userRepository.save(u);
                    return new AuthResponse(true, "임시 비밀번호가 발급되었습니다.", tempPw);
                })
                .orElse(new AuthResponse(false, "일치하는 정보가 없습니다."));
    }
}
