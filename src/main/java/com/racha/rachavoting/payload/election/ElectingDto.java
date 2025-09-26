package com.racha.rachavoting.payload.election;

import java.util.Set;

import com.racha.rachavoting.payload.party.PartyBasicDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectingDto {
    private String title;

    private String year;

    private Set<PartyBasicDTO> party;
}
