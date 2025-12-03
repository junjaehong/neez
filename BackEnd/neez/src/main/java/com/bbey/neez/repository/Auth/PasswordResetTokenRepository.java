package com.bbey.neez.repository.Auth;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbey.neez.entity.Auth.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    void deleteByEmail(String email);

    Optional<PasswordResetToken> findByEmailAndCode(String email, String code);
}
