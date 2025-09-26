package com.racha.rachavoting.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racha.rachavoting.model.components.RegistrationSequence;
import com.racha.rachavoting.repository.RegistrationSequenceRepository;

@Service
@Transactional
public class RegistrationSequenceService {
    
    @Autowired
    private RegistrationSequenceRepository registrationSequenceRepository;

    public RegistrationSequence save(RegistrationSequence registrationSequence) {
        return registrationSequenceRepository.save(registrationSequence);
    }

    public RegistrationSequence getCurrentRegistrationSequence(int year, String type) {
        RegistrationSequence sequence = registrationSequenceRepository.findByYearAndType(year, type);
        if (sequence == null) {
            sequence = new RegistrationSequence();
            sequence.setYear(year);
            sequence.setType(type);
            sequence.setCounter(0L); // Initialize counter to 0
            sequence = registrationSequenceRepository.save(sequence);
        }
        return sequence;
    }
}
