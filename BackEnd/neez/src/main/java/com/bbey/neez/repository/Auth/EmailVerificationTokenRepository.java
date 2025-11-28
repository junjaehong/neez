package com.bbey.neez.repository.Auth;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbey.neez.entity.Auth.EmailVerificationToken;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    void deleteByToken(String token);
}
