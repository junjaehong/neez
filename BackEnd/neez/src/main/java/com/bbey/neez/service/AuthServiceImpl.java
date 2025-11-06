package com.bbey.neez.service;

import com.bbey.neez.entity.Users;
import com.bbey.neez.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ 회원가입
    @Override
    public String register(String userId, String password, String name, String email) {
        if (userRepository.findByUserId(userId).isPresent()) {
            return "⚠️ 이미 존재하는 아이디입니다.";
        }

        Users u = new Users();
        u.setUserId(userId);
        u.setPassword(password);
        u.setName(name);
        u.setEmail(email);
        u.setCreated_at(LocalDateTime.now());
        u.setUpdated_at(LocalDateTime.now());
        userRepository.save(u);
        return "✅ 회원가입 성공: " + userId;
    }

    // ✅ 로그인
    @Override
    public String login(String userId, String password) {
        Optional<Users> optUser = userRepository.findByUserId(userId);
        if (!optUser.isPresent()) return "❌ 존재하지 않는 아이디입니다.";
        Users user = optUser.get();

        if (!user.getPassword().equals(password))
            return "❌ 비밀번호가 일치하지 않습니다.";

        return "✅ 로그인 성공: " + user.getName();
    }

    // ✅ 로그아웃
    @Override
    public String logout(String userId) {
        return "👋 로그아웃 완료: " + userId;
    }

    // ✅ 회원탈퇴
    @Override
    public String delete(String userId, String password) {
        Optional<Users> optUser = userRepository.findByUserId(userId);
        if (!optUser.isPresent()) return "❌ 존재하지 않는 사용자입니다.";
        Users user = optUser.get();

        if (!user.getPassword().equals(password))
            return "❌ 비밀번호가 일치하지 않습니다.";

        userRepository.delete(user);
        return "🗑 회원탈퇴 완료: " + userId;
    }

    // ✅ 아이디 찾기
    @Override
    public String findUserId(String name, String email) {
        return userRepository.findAll().stream()
                .filter(u -> name.equals(u.getName()) && email.equals(u.getEmail()))
                .findFirst()
                .map(u -> "✅ 아이디: " + u.getUserId())
                .orElse("❌ 일치하는 사용자가 없습니다.");
    }

    // ✅ 비밀번호 찾기 (임시 비밀번호 생성)
    @Override
    public String resetPassword(String userId, String email) {
        return userRepository.findByUserId(userId)
                .filter(u -> email.equals(u.getEmail()))
                .map(u -> {
                    String tempPw = "pw" + (int)(Math.random() * 9000 + 1000);
                    u.setPassword(tempPw);
                    u.setUpdated_at(LocalDateTime.now());
                    userRepository.save(u);
                    return "✅ 임시 비밀번호가 발급되었습니다: " + tempPw;
                })
                .orElse("❌ 일치하는 정보가 없습니다.");
    }
}
