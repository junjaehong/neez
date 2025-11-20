package com.bbey.neez.repository;

import com.bbey.neez.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    void deleteByEmail(String email);

    Optional<PasswordResetToken> findByEmailAndCode(String email, String code);
}
