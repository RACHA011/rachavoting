package com.racha.rachavoting.payload.vote;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor 
public class CastVoteDto {
    @NotBlank(message = "Election ID is required")
    private String electionId;
    
    @NotBlank(message = "Party ID is required")
    private String partyId;
}
