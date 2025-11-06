package com.bbey.neez.service;

import com.bbey.neez.entity.Users;
import com.bbey.neez.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String register(String userId, String password, String name, String email) {
        if (userRepository.findByUserId(userId).isPresent()) {
            return "⚠️ 이미 존재하는 아이디입니다.";
        }

        Users user = new Users();
        user.setUserId(userId);
        user.setPassword(password);
        user.setName(name);
        user.setEmail(email);
        user.setCreated_at(LocalDateTime.now());
        user.setUpdated_at(LocalDateTime.now());
        userRepository.save(user);

        return "✅ 회원가입 성공: " + userId;
    }

    @Override
    public String login(String userId, String password) {
        return userRepository.findByUserId(userId)
                .map(user -> user.getPassword().equals(password)
                        ? "✅ 로그인 성공: " + user.getUserId()
                        : "❌ 비밀번호가 틀렸습니다.")
                .orElse("❌ 존재하지 않는 사용자입니다.");
    }
}
