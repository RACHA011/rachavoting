package com.racha.rachavoting.model;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class RegisteredEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(nullable = false, updatable = false, unique = true)
    // private String emailHash; // SHA-256 hash of email for lookup

    @Column(nullable = false, unique = true)
    private String unsubscribeToken;

    @Column(nullable = false, updatable = false, unique = false)
    private String encryptedEmail;

    @Column(nullable = false, updatable = false)
    private String year;

    @Column(nullable = false)
    private boolean subscribed = true;

    @Column(nullable = false)
    private List<String> electionId;

    // @Column(nullable = false, updatable = false)
    private Instant tokenExpiry; // for now i wont be using this

}
