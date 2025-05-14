package com.racha.rachavoting.payload.candidate;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CandidatePrivateDTO extends CandidatePublicDTO {
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private boolean isValid;
    private boolean isActive;
    private String status;
}