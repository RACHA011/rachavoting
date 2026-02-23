package com.racha.rachavoting.repository;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.racha.rachavoting.model.Election;

@Repository
public interface ElectionRepository extends JpaRepository<Election, Long> {

    List<Election> findByYear(String year);

    Page<Election> findByPublicAccessKeyIsNull(Pageable pageable);

    Optional<Election> findByPublicAccessKey(String publicAccessKey);

    @Query("SELECT e FROM Election e WHERE " +
            "e.startDate BETWEEN :startRange AND :endRange OR " +
            "e.endDate BETWEEN :startRange AND :endRange")
    List<Election> findElectionsNeedingStatusUpdate(
            @Param("startRange") LocalDateTime startRange,
            @Param("endRange") LocalDateTime endRange);

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime start, LocalDateTime end);

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
