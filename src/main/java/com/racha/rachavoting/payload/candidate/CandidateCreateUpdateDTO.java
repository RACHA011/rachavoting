package com.racha.rachavoting.payload.candidate;

import java.time.LocalDate;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateCreateUpdateDTO {
    private String fullName;
    private String email;
    private String password;
    private String profilePicture;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String state;
    private Long partyId;
    private boolean isPresidentialCandidate;
    private String manifestoUrl;
    private Set<Long> authorityIds;
}