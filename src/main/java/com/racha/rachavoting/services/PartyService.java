package com.racha.rachavoting.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.racha.rachavoting.model.Party;
import com.racha.rachavoting.repository.PartyRepository;

@Service
public class PartyService {
    @Autowired
    private PartyRepository partyRepository;

    public Party saveParty(Party party) {
        return partyRepository.save(party);
    }

    public Party getPartyById(Long id) {
        return partyRepository.findById(id).orElse(null);
    }

    public Party findPartyByName(String name) {
        return partyRepository.findByName(name).orElse(null);
    }

}
