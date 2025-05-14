package com.racha.rachavoting.model;

import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Account  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username is required")
    private String username;  // For login (not shown publicly)

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    private String email; 

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    private String password;  // BCrypt hashed

    @Column(nullable = false)
    private boolean isAdmin;  

}