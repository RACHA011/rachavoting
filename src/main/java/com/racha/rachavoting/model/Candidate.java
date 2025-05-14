package com.racha.rachavoting.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName; // Candidate's Full Name

    @Column(unique = true, nullable = false)
    private String email; // Candidate's email

    @Column(nullable = false)
    private String password; // Candidate's password

    private String hashedId;

    private String profilePicture;

    private String phoneNumber;

    @Column(unique = true)
    private String membershipId;

    private LocalDate dateOfBirth; // Candidate's Date of Birth

    private String state; // State/Region of Candidate

    @ManyToOne
    @JoinColumn(name = "party_id", nullable = true)
    private Party party; // Party to which the candidate belongs

    @Column(nullable = false)
    private boolean isValid = false;

    private boolean isActive = false;

    private String status;

    private boolean isPresidentialCandidate = false; // True if running for president

    private String manifestoUrl; // Link to the candidate's political manifesto (optional)

    private Set<Long> authorityIds = new HashSet<>();

    @PostLoad
    private void initialize() {
        if (authorityIds == null) {
            authorityIds = new HashSet<>();
        }
    }
}
