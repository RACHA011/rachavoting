package com.racha.rachavoting.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.racha.rachavoting.model.Voter;
import com.racha.rachavoting.payload.vote.VotingDto;
import com.racha.rachavoting.payload.voter.VoterCreateDTO;
import com.racha.rachavoting.payload.voter.VoterViewDTO;

@Component
public class VoterMapper {

    @Autowired
    private ElectionMapper electionMapper;

    public VoterViewDTO toPublicView(Voter voter) {
        if (voter == null) {
            return null;
        }
        VoterViewDTO dto = new VoterViewDTO();
        dto.setRegNo(voter.getRegestrationNo());
        dto.setRegistrationDate(voter.getRegistrationDate().toLocalDate());
        dto.setProvince(voter.getProvince());
        dto.setDistrict(voter.getDistrict());
        dto.setMunicipality(voter.getMunicipality());
        dto.setTown(voter.getTown());

        return dto;
    }

    public Voter createVote(VoterCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        Voter voter = new Voter();
        voter.setHashedNationalId(dto.getNationalId());
        voter.setProvince(dto.getProvince().toLowerCase());
        voter.setDistrict(dto.getDistrict().toLowerCase());
        voter.setMunicipality(dto.getMunicipality().toLowerCase());
        voter.setTown(dto.getTown().toLowerCase());

        return voter;
    }

    public VotingDto toVotingDto(Voter voter) {
        if (voter == null) {
            return null;
        }

        VotingDto dto = new VotingDto();
        dto.setRegNo(voter.getRegestrationNo());
        dto.setProvince(voter.getProvince());
        dto.setDistrict(voter.getDistrict());
        // Assuming you have a method to convert elections to ElectingDto
        dto.setElection(electionMapper.toElectingDtoList(voter.getElections()));

        return dto;
    }

}
