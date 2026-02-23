package com.racha.rachavoting.services;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racha.rachavoting.mapper.PartyMapper;
import com.racha.rachavoting.model.Party;
import com.racha.rachavoting.payload.party.PartyBasicDTO;
import com.racha.rachavoting.repository.PartyRepository;

@Service
public class PartyService {
    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private PartyMapper partyMapper;

    public Party saveParty(Party party) {
        return partyRepository.save(party);
    }

    public Party getPartyById(Long id) {
        return partyRepository.findById(id).orElse(null);
    }

    public Party findPartyByName(String name) {
        return partyRepository.findByName(name).orElse(null);
    }

    @Transactional(readOnly = true)
    public Set<PartyBasicDTO> getAllParties() {
        List<Party> parties = partyRepository.findAll();

        Set<PartyBasicDTO> partyDTOs = parties.stream()
                .map(partyMapper::toBasicDto)
                .collect(Collectors.toSet());

        return partyDTOs;
    }
}
