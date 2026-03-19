package com.om.smartpost.auth.repository;

import com.om.smartpost.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {
    Optional<PasswordResetToken> findByEmailAndOtpAndUsedFalse(String email, Integer otp);
    Optional<PasswordResetToken> findTopByEmailOrderByIdDesc(String email);
}

