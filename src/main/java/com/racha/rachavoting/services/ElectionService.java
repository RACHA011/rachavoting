package com.racha.rachavoting.services;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racha.rachavoting.model.Election;
import com.racha.rachavoting.repository.ElectionRepository;

@Service
@Transactional(readOnly = true)
public class ElectionService {

    @Autowired
    private ElectionRepository electionRepository;

    public void saveElection(Election election) {
        election.setUpdatedAt(LocalDateTime.now());
        electionRepository.save(election);
    }

    public Election getElectionById(Long id) {
        return electionRepository.findById(id).orElse(null);
    }

    public Election findElectionByYear(String year) {
        return electionRepository.findByYear(year).orElse(null);
    }

    public boolean existsByYear(String year) {
        return electionRepository.existsByYear(year);
    }

    // Check for overlapping dates
    public boolean hasDateOverlap(LocalDateTime start, LocalDateTime end) {
        return electionRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start);
    }

    public List<Election> getAllElections() {
        return electionRepository.findAll();
    }

    public long electionCount() {
        return electionRepository.count();
    }

    public long getActiveElection() {
        return electionRepository.countByIsActiveTrue();
    }

    public List<Election> getWhereIsActiveTrue() {
        return electionRepository.findByIsActiveTrue();
    }

    public List<Election> getIsActiveTrueorCurrentyear() {
        return electionRepository.findActiveOrCurrentYearElections();
    }

    // Generate a tamper-proof hash (for blockchain)
    public String generateElectionHash(Election election) {
        String data = election.getTitle() + election.getYear()
                + election.getStartDate() + election.getEndDate();
        return DigestUtils.sha256Hex(data); // Apache Commons Codec
    }

    @Scheduled(fixedRate = 60000) // Runs every minute to update the is active state
    public void updateElectionStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Election> elections = electionRepository.findAll();

        elections.forEach(election -> {
            boolean shouldBeActive = (now.isEqual(election.getStartDate()) || now.isAfter(election.getStartDate()))
                    && now.isBefore(election.getEndDate());
            // System.out.println(shouldBeActive + "  " + election.isActive() + " " + election.getTitle());
            if (election.isActive() != shouldBeActive) {
                election.setActive(shouldBeActive);
                electionRepository.save(election);
            }
        });
    }
}
