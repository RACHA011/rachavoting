package com.racha.rachavoting.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.racha.rachavoting.model.Voter;


@Repository
public interface VoterRepository extends JpaRepository<Voter, String> {

    boolean existsByHashedNationalId(String hashedNationalId); // Check if a voter with the given national ID exists

    Optional<Voter> findByRegestrationNo(String regestrationNo);
    Optional<Voter> findByUniqueId(String uniqueId);

    Voter findByHashedNationalId(String hashedNationalId); // Find a voter by their national ID
}
