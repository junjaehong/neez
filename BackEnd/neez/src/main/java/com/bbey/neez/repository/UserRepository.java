package com.bbey.neez.repository;

import com.bbey.neez.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    // 회원정보 수정/조회 시 사용
    Optional<Users> findById(Long idx);

    // ✅ 로그인 시 사용
    Optional<Users> findByUserId(String userId);

    // ✅ 아이디 찾기 시 이름+이메일로 사용자 찾기
    Optional<Users> findByNameAndEmail(String name, String email);

    // ✅ 비밀번호 찾기 시 아이디+이메일로 사용자 찾기
    Optional<Users> findByUserIdAndEmail(String userId, String email);

     // 비밀번호 재설정 단계 2에서 필요
    Optional<Users> findByEmail(String email);
}
