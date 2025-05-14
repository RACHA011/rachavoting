package com.racha.rachavoting.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.racha.rachavoting.model.Authority;
import com.racha.rachavoting.model.Candidate;
import com.racha.rachavoting.repository.CandidateRepository;
import com.racha.rachavoting.util.constants.AuthorityEnums;

@Service
public class CandidateService implements UserDetailsService {
    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthorityService authorityService;

    public void save(Candidate candidate) {
        candidateRepository.save(candidate);
    }

    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id).orElse(null);
    }

    public Candidate getCandidateByEmail(String email) {
        return candidateRepository.findByEmailIgnoreCase(email).orElse(null);
    }

    public boolean existsByEmail(String email) {
        return candidateRepository.existsByEmail(email);
    }

    public Candidate createCandidateAccount(Candidate candidate) {
        if (emailExists(candidate.getEmail())) {
            throw new IllegalArgumentException("candidate already exists");
        }
        candidate.setPassword(passwordEncoder.encode(candidate.getPassword()));
        return candidateRepository.save(candidate);
    }

    public long candidateCount() {
        return candidateRepository.count();
    }

    public Page<Candidate> searchCandidates(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return candidateRepository.findAll(pageable);
        }
        return candidateRepository.searchByNameOrEmail(searchTerm, pageable);
    }

    public long candidateCountStatusTrue() {
        return candidateRepository.countByIsActiveTrue();
    }

    public List<Candidate> getNonVarifiedPresidents() {
        return candidateRepository.findByIsValidFalseAndIsPresidentialCandidateTrue();
    }

    @Order(2)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Candidate candidate = candidateRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Candidate not found with Email: " + email));

        String authority = AuthorityEnums.ROLE_CANDIDATE.name();

        List<GrantedAuthority> grantedAuthority = new ArrayList<>();

        grantedAuthority.add(new SimpleGrantedAuthority(authority));

        for (Long _auth : candidate.getAuthorityIds()) {
            Optional<Authority> authorities = authorityService.findById(_auth);
            if (authorities.isPresent()) {
                grantedAuthority.add(new SimpleGrantedAuthority(authorities.get().getName()));
            }
        }

        return new User(
                candidate.getEmail(),
                candidate.getPassword(),
                true, true, true, true,
                grantedAuthority);
    }

    public boolean emailExists(String email) {
        return candidateRepository.existsByEmail(email);
    }
}
