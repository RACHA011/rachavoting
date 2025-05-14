package com.racha.rachavoting.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import com.racha.rachavoting.model.Candidate;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    boolean existsByEmail(@NonNull String email);

    @Query("SELECT c FROM Candidate c WHERE " +
            "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Candidate> searchByNameOrEmail(@Param("searchTerm") String searchTerm, Pageable pageable);

    @NonNull
    Page<Candidate> findAll(@NonNull Pageable pageable);

    Optional<Candidate> findByEmailIgnoreCase(@NonNull String email);

    long countByIsActiveTrue();

    @NonNull
    List<Candidate> findByIsValidFalseAndIsPresidentialCandidateTrue();

}