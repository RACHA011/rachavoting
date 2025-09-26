package com.racha.rachavoting.mapper;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.racha.rachavoting.model.Party;
import com.racha.rachavoting.payload.party.PartyBasicDTO;
import com.racha.rachavoting.payload.party.PartyCreateUpdateDTO;
import com.racha.rachavoting.payload.party.PartyDetailsDTO;

@Component
public class PartyMapper {

    @Autowired
    private CandidateMapper candidateMapper;

    public PartyBasicDTO toBasicDto(Party party) {
        PartyBasicDTO dto = new PartyBasicDTO();
        dto.setId(party.getId());
        dto.setName(party.getName());
        dto.setAbbreviation(party.getAbbreviation());
        dto.setLeader(party.getLeader());
        dto.setSlogan(party.getSlogan());
        dto.setLogoUrl(party.getLogoUrl());
        dto.setColors(party.getColors());
        return dto;
    }

    public PartyDetailsDTO toDetailsDto(Party party) {
        PartyDetailsDTO dto = new PartyDetailsDTO();
        // Map all basic fields
        toBasicDto(party); // Reuse basic mapping

        // Map additional fields
        dto.setDescription(party.getDescription());
        dto.setColors(party.getColors());
        dto.setFoundingDate(party.getFoundingDate());
        dto.setWebsite(party.getWebsite());
        dto.setRegistered(party.isRegistered());
        dto.setHeadquarters(party.getHeadquarters());
        dto.setRegistrationNo(party.getRegistrationNo());
        dto.setFacebook(party.getFacebook());
        dto.setX(party.getX());
        dto.setInstagram(party.getInstagram());
        dto.setYoutube(party.getYoutube());

        // Map candidates using CandidateMapper
        if (party.getCandidates() != null) {
            dto.setCandidates(party.getCandidates().stream()
                    .map(candidateMapper::toPublicDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public Party fromCreateUpdateDto(PartyCreateUpdateDTO dto) {
        Party party = new Party();
        party.setName(dto.getName());
        party.setAbbreviation(dto.getAbbreviation());
        party.setLeader(dto.getLeader());
        party.setSlogan(dto.getSlogan());
        party.setDescription(dto.getDescription());
        party.setColors(dto.getColors());
        party.setFoundingDate(dto.getFoundingDate());
        party.setLogoUrl(dto.getLogoUrl());
        party.setWebsite(dto.getWebsite());
        party.setHeadquarters(dto.getHeadquarters());
        party.setRegistrationNo(dto.getRegistrationNo());
        party.setFacebook(dto.getFacebook());
        party.setX(dto.getX());
        party.setInstagram(dto.getInstagram());
        party.setYoutube(dto.getYoutube());
        return party;
    }
}