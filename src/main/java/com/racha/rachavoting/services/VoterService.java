package com.racha.rachavoting.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.racha.rachavoting.repository.VoterRepository;

@Service
public class VoterService {
    @Autowired
    private VoterRepository voterRepository; // Assuming you have a VoterRepository

    public boolean existsByHashedNationalId(String hashedNationalId) {
        return voterRepository.existsByHashedNationalId(hashedNationalId);
    }
}
