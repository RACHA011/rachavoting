package com.racha.rachavoting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.racha.rachavoting.model.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    // Custom query methods can be defined here if needed
    // For example, to find votes by candidate name or voter ID
   
} 
