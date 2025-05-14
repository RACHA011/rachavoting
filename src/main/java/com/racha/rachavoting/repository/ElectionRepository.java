package com.racha.rachavoting.repository;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.racha.rachavoting.model.Election;

@Repository
public interface ElectionRepository extends JpaRepository<Election, Long> {

    Optional<Election> findByYear(String year);

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime start, LocalDateTime end);

    boolean existsByYear(String year);

    List<Election> findByIsActiveTrue();

    @Query("SELECT e FROM Election e WHERE e.isActive = true OR e.year = :currentYear")
    List<Election> findActiveOrCurrentYearElections(String currentYear);

    // Convenience default method that automatically uses the current year
    default List<Election> findActiveOrCurrentYearElections() {
        String currentYear = String.valueOf(Year.now().getValue());
        return findActiveOrCurrentYearElections(currentYear);
    }

    long countByIsActiveTrue();

}
