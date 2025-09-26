package com.racha.rachavoting.payload.vote;

import java.util.Set;

import com.racha.rachavoting.payload.election.ElectingDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VotingDto {
    private String regNo;

    private String province;

    private String district;

    private Set<ElectingDto> election;
}
