package com.racha.rachavoting.mapper;

import org.springframework.stereotype.Component;

import com.racha.rachavoting.model.Candidate;
import com.racha.rachavoting.payload.candidate.CandidateAdminDTO;
import com.racha.rachavoting.payload.candidate.CandidateCreateUpdateDTO;
import com.racha.rachavoting.payload.candidate.CandidatePrivateDTO;
import com.racha.rachavoting.payload.candidate.CandidatePublicDTO;

@Component
public class CandidateMapper {

    public CandidatePublicDTO toPublicDto(Candidate candidate) {
        if (candidate == null) {
            return null;
        }

        CandidatePublicDTO dto = new CandidatePublicDTO();
        dto.setId(candidate.getId());
        dto.setFullName(candidate.getFullName());
        dto.setProfilePicture(candidate.getProfilePicture());
        dto.setState(candidate.getState());
        dto.setManifestoUrl(candidate.getManifestoUrl());

        if (candidate.getParty() != null) {
            dto.setPartyId(candidate.getParty().getId());
            dto.setPartyName(candidate.getParty().getName());
        }

        return dto;
    }

    public CandidatePrivateDTO toPrivateDto(Candidate candidate) {
        if (candidate == null) {
            return null;
        }

        CandidatePrivateDTO dto = new CandidatePrivateDTO();
        // Copy all public fields
        CandidatePublicDTO publicDto = toPublicDto(candidate);
        dto.setId(publicDto.getId());
        dto.setFullName(publicDto.getFullName());
        dto.setProfilePicture(publicDto.getProfilePicture());
        dto.setState(publicDto.getState());
        dto.setPartyId(publicDto.getPartyId());
        dto.setPartyName(publicDto.getPartyName());
        dto.setManifestoUrl(publicDto.getManifestoUrl());

        // Add private fields
        dto.setEmail(candidate.getEmail());
        dto.setPhoneNumber(candidate.getPhoneNumber());
        dto.setDateOfBirth(candidate.getDateOfBirth());
        dto.setValid(candidate.isValid());
        dto.setActive(candidate.isActive());
        dto.setStatus(candidate.getStatus());

        return dto;
    }

    public CandidateAdminDTO toAdminDto(Candidate candidate) {
        if (candidate == null) {
            return null;
        }

        CandidateAdminDTO dto = new CandidateAdminDTO();
        // Copy all private fields
        CandidatePrivateDTO privateDto = toPrivateDto(candidate);
        dto.setId(privateDto.getId());
        // ... copy all other fields from privateDto ...

        // Add admin-only fields
        dto.setAuthorityIds(candidate.getAuthorityIds());

        return dto;
    }

    public Candidate fromCreateUpdateDto(CandidateCreateUpdateDTO dto) {
        if (dto == null) {
            return null;
        }

        Candidate candidate = new Candidate();
        candidate.setFullName(dto.getFullName());
        candidate.setEmail(dto.getEmail());
        candidate.setPassword(dto.getPassword()); // Note: Should be encoded
        candidate.setProfilePicture(dto.getProfilePicture());
        candidate.setPhoneNumber(dto.getPhoneNumber());
        candidate.setDateOfBirth(dto.getDateOfBirth());
        candidate.setState(dto.getState());
        candidate.setPresidentialCandidate(dto.isPresidentialCandidate());
        candidate.setManifestoUrl(dto.getManifestoUrl());
        candidate.setAuthorityIds(dto.getAuthorityIds());

        // Party will be set separately via ID
        return candidate;
    }

    public void updateFromDto(CandidateCreateUpdateDTO dto, Candidate candidate) {
        if (dto == null || candidate == null) {
            return;
        }

        candidate.setFullName(dto.getFullName());
        if (dto.getEmail() != null) {
            candidate.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null) {
            candidate.setPassword(dto.getPassword()); // Note: Should be encoded
        }
        candidate.setProfilePicture(dto.getProfilePicture());
        candidate.setPhoneNumber(dto.getPhoneNumber());
        candidate.setDateOfBirth(dto.getDateOfBirth());
        candidate.setState(dto.getState());
        candidate.setPresidentialCandidate(dto.isPresidentialCandidate());
        candidate.setManifestoUrl(dto.getManifestoUrl());
        if (dto.getAuthorityIds() != null) {
            candidate.setAuthorityIds(dto.getAuthorityIds());
        }
    }
}