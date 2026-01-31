package com.racha.rachavoting.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class Party {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Party Name (e.g., "African National Congress")

    private String ideology;

    @Column(unique = true, nullable = false)
    private String abbreviation; // Party abrevation (e.g., "ANC")

    @Column(nullable = false)
    private String leader; // Party Leader's Name

    @Column(nullable = false)
    private String slogan;

    @Column(nullable = false)
    private String description;

    private List<String> colors = new ArrayList<>();

    private boolean status = false;

    private LocalDate FoundingDate;

    private String logoUrl;

    private String website; // Official website (optional)

    private boolean isRegistered; // Whether the party is officially registered

    private String headquarters;

    private String registrationNo;

    // social media handles
    private String facebook;

    private String x;

    private String instagram;

    private String youtube;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Candidate> candidates; // List of Candidates in this Party

    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "party_election", joinColumns = @JoinColumn(name = "party_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "election_id"))
    private Set<Election> elections = new HashSet<>();
}
