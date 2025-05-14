package com.racha.rachavoting.model;

import java.time.LocalDateTime;

import com.racha.rachavoting.annotations.interfaces.NationalIdNotRegistered;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class Voter {
    @Id
    @Column(length = 100, unique = true, nullable = false, updatable = false)
    @NationalIdNotRegistered  
    private String hashedNationalId; // SHA-256 hash

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String municipality;

    @Column(nullable = false)
    private String town;

    @Column(nullable = false)
    private boolean hasVoted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @Column(nullable = false)
    private boolean isVerified = false;

    @Column(nullable = false)
    private LocalDateTime registrationDate = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String encryptedMetadata; // AES-256 encrypted JSON

    @OneToOne(mappedBy = "voter", cascade = CascadeType.ALL)
    private Vote vote;
}