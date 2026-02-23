package com.racha.rachavoting.model.components;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class RegistrationSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private int year;

    @Column(nullable = false)
    private Long counter = 0L; // Counter for the current registration sequence

    @Column(nullable = false, updatable = false)
    private String type; // optional, can be VOTER, ELECTION, etc.

}
