package com.racha.rachavoting.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.racha.rachavoting.model.Party;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
    // Custom query methods can be defined here if needed
    // For example, find by party name or other attributes
    Optional<Party> findByName(String name);
    
}
