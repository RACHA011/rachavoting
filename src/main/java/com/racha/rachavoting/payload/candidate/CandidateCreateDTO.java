package com.racha.rachavoting.payload.candidate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new candidate
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateCreateDTO {
    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    // @Min(value = 18, message = "Candidate must be at least 18 years old")
    // @Max(value = 120, message = "Age value is not valid")
    // private int age;

    // private boolean isPresidentialCandidate;

    @NotBlank(message = "Email address is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    private String partyId;

    @NotBlank(message = "password is required")
    @Min(value = 8, message = "Password must be at least 8 characters long")
    @Max(value = 20, message = "Password must be at most 20 characters long")
    // @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
    //          message = "Password must contain at least one uppercase, one lowercase, and one number")
    private String password;
    
    @NotBlank(message = "Please confirm your password")
    @Min(value = 8, message = "Password must be at least 8 characters long")
    @Max(value = 20, message = "Password must be at most 20 characters long")
    private String confirmPassword;

    
}
