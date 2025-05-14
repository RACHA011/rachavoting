package com.racha.rachavoting.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.racha.rachavoting.model.Authority;
import com.racha.rachavoting.repository.AuthorityRepository;

@Service
public class AuthorityService {
    
    @Autowired
    private AuthorityRepository authorityRepository;

    public Authority save(Authority authority) {
        return authorityRepository.save(authority);
    }

    public Optional<Authority> findById(Long id) {
        return authorityRepository.findById(id);
    }
    
    public Optional<Authority> findByName(String Name) {
        return authorityRepository.findByName(Name);
    }
    
}
