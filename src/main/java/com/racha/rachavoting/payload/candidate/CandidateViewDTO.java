package com.racha.rachavoting.payload.candidate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateViewDTO {

    private Long id;
    
    private String fullName; // Candidate's Full Name

    private String email; // Candidate's email

    private String status;

    private boolean isActive;

    private String profilePicture;

    private boolean isValid;

    private boolean isPresidentialCandidate; // True if running for president


}
