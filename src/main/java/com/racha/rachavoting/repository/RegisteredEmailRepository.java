package com.racha.rachavoting.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.racha.rachavoting.model.RegisteredEmail;

@Repository
public interface RegisteredEmailRepository extends JpaRepository<RegisteredEmail, Long> {
    Optional<RegisteredEmail> findByEncryptedEmail(String encryptedEmail);
    Optional<RegisteredEmail> findByUnsubscribeToken(String unsubscribeToken);
} 
