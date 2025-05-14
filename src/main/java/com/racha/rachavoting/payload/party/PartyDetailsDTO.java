package com.racha.rachavoting.payload.party;

import java.time.LocalDate;
import java.util.List;

import com.racha.rachavoting.payload.candidate.CandidatePublicDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PartyDetailsDTO extends PartyBasicDTO {
    private String description;
    private List<String> colors;
    private LocalDate foundingDate;
    private String website;
    private boolean isRegistered;
    private String headquarters;
    private String registrationNo;
    private String facebook;
    private String x;
    private String instagram;
    private String youtube;
    private List<CandidatePublicDTO> candidates; // Use a Candidate DTO too!
}