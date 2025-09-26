package com.racha.rachavoting.model;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@Data
@Entity
public class Voter {
    @Id
    @Column(length = 100, unique = true, nullable = false, updatable = false)
    private String hashedNationalId; // SHA-256 hash

    @Column(unique = true, nullable = false)
    private String hashedId;

    @Column(unique = true)
    private String uniqueId;

    @Column(nullable = false, unique = true, updatable = false)
    private String regestrationNo;

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

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "election_id", nullable = false)
    // private Election election;

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "voter_election", joinColumns = @JoinColumn(name = "voter_id", referencedColumnName = "hashedNationalId"), inverseJoinColumns = @JoinColumn(name = "election_id"))
    private Set<Election> elections = new HashSet<>();

    @Column(nullable = false)
    private boolean isVerified = false;

    @Column(nullable = false)
    private LocalDateTime registrationDate = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String encryptedMetadata; // AES-256 encrypted JSON

    // @OneToOne(mappedBy = "voter", cascade = CascadeType.ALL)
    // private Vote vote;

    @OneToMany(mappedBy = "voter", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Vote> votes = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void updateId() {
        if (this.uniqueId == null || this.uniqueId.isEmpty() || this.uniqueId.isBlank()) {
            this.uniqueId = generateUniqueId();
        }
    }

    private String generateUniqueId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256-bit key

        // Generate random bytes
        random.nextBytes(bytes);

        // Use URL-safe Base64 encoding without padding
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes)
                .replaceAll("[^a-zA-Z0-9]", "");
    }

    // Proper equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Voter))
            return false;
        Voter voter = (Voter) o;
        return hashedNationalId != null && hashedNationalId.equals(voter.hashedNationalId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Helper methods for managing relationships
    public void addElection(Election election) {
        this.elections.add(election);
        election.getVoters().add(this);
    }

    public void removeElection(Election election) {
        this.elections.remove(election);
        election.getVoters().remove(this);
    }

}