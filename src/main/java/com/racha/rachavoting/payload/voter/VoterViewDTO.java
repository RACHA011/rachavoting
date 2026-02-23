package com.racha.rachavoting.payload.voter;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoterViewDTO {
    private String regNo;  
    private String province;
    private String district;
    private String municipality;
    private String town;
    private LocalDate registrationDate;
}
