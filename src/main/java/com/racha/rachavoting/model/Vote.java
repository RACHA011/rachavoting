package com.racha.rachavoting.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", referencedColumnName = "hashedNationalId")
    private Voter voter;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedVote; // AES-256 encrypted candidate choice

    @Column(nullable = false, length = 64)
    private String voteHash; // SHA-256(hashedNationalId + encryptedVote)

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}