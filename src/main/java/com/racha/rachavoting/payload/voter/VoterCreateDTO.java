package com.racha.rachavoting.payload.voter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoterCreateDTO {
    @NotBlank
    @Pattern(regexp = "^[0-9]{13}$")
    private String nationalId; 
    
    @NotBlank
    private String province;

    @NotBlank
    private String district;
    
    @NotBlank
    private String municipality;
    
    @NotBlank
    private String town;
}
