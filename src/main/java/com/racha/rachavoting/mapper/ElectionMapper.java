package com.racha.rachavoting.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.racha.rachavoting.model.Election;
import com.racha.rachavoting.payload.election.ElectingDto;
import com.racha.rachavoting.payload.election.ElectionPublicDTO;
import com.racha.rachavoting.payload.party.PartyBasicDTO;
import com.racha.rachavoting.services.PartyService;

@Component
public class ElectionMapper {

    @Autowired
    private PartyService partyService;

    public ElectionPublicDTO toPublicDTO(Election election) {
        if (election == null) {
            return null;
        }

        return ElectionPublicDTO.builder()
                .publicAccessKey(election.getPublicAccessKey())
                .title(election.getTitle())
                .description(election.getDescription())
                .startDate(election.getStartDate())
                .year(election.getYear())
                .endDate(election.getEndDate())
                .registrationDeadline(election.getRegistrationDeadline())
                .isActive(election.isActive())
                .resultAnouncement(election.getResultAnouncement())
                .build();

    }

    public ElectingDto toElectingDto(Election election) {
        if (election == null) {
            return null;
        }
        
        Set<PartyBasicDTO> parties = partyService.getAllParties();

        return ElectingDto.builder()
                .title(election.getTitle())
                .year(election.getYear())
                .party(parties)
                .build();
    }

    public Set<ElectingDto> toElectingDtoList(Set<Election> elections) {
        if (elections == null) {
            return null;
        }
        return elections.stream().map(this::toElectingDto).collect(Collectors.toSet());
    }

}
