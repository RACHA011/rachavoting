package com.racha.rachavoting.model;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@Data
@Entity
public class Election {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String publicAccessKey;

    @Column(nullable = false)
    private String title;

    // @Column(nullable = false, unique = true)
    @Column(nullable = false)
    private String year;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    private LocalDateTime resultAnouncement;

    @Column(nullable = false)
    private LocalDateTime registrationDeadline;

    @Column(nullable = false)
    private boolean isActive;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(nullable = false)
    private BigInteger registeredVoters = new BigInteger("0");

    @Column(nullable = false)
    private BigInteger totalVotes = new BigInteger("0");

    // @OneToMany(mappedBy = "election", cascade = CascadeType.ALL, orphanRemoval =
    // true)
    // private List<Voter> voters;

    @ManyToMany(mappedBy = "elections", fetch = FetchType.LAZY)
    private Set<Voter> voters = new HashSet<>();

    // @OneToMany(mappedBy = "election", cascade = CascadeType.ALL, orphanRemoval =
    // true)
    // private List<Vote> votes;

    @OneToMany(mappedBy = "election", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Vote> votes = new HashSet<>();

    @Column(nullable = false)
    private boolean voterRegistrationOpen = false;

    @Column(nullable = false)
    private LocalDateTime systemUpdate = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    private void generateAccessKey() {
        if (this.publicAccessKey == null || this.publicAccessKey.isEmpty() || this.publicAccessKey.length() < 32) {
            this.systemUpdate = LocalDateTime.now();
            this.publicAccessKey = generateSecureRandomKey();
        }
    }

    private String generateSecureRandomKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256-bit key

        // Generate random bytes
        random.nextBytes(bytes);

        // Use URL-safe Base64 encoding without padding
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes)
                .replaceAll("[^a-zA-Z0-9]", ""); // Extra safety to remove any non-alphanumeric chars
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Election)) return false;
        Election election = (Election) o;
        return id != null && id.equals(election.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}