package com.racha.rachavoting.mapper;

import org.springframework.stereotype.Component;

import com.racha.rachavoting.model.Election;
import com.racha.rachavoting.payload.election.ElectionPublicDTO;

@Component
public class ElectionMapper {

    public ElectionPublicDTO toPublicDTO(Election election) {
        if (election == null) {
            return null;
        }

        return ElectionPublicDTO.builder()
                .id(election.getId())
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
}
