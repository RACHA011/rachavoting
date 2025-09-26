package com.racha.rachavoting.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racha.rachavoting.model.Election;
import com.racha.rachavoting.repository.ElectionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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

    public Election findByPublicKey(String publicKey) {
        return electionRepository.findByPublicAccessKey(publicKey)
                .orElseThrow(null);
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

    @Transactional(readOnly = true)
    public List<Election> getIsActiveTrueorCurrentyear() {
        return electionRepository.findActiveOrCurrentYearElections();
    }

    // Generate a tamper-proof hash (for blockchain)
    public String generateElectionHash(Election election) {
        String data = election.getTitle() + election.getYear()
                + election.getStartDate() + election.getEndDate();
        return DigestUtils.sha256Hex(data); // Apache Commons Codec
    }

    public List<Election> getElectionsByYear(String year) {
        return electionRepository.findByYear(year);
    }

    @Transactional(readOnly = true)
    public List<Election> getElectionsByYearWithInitializedCollections(String year) {
        List<Election> elections = electionRepository.findByYear(year);
        // Initialize collections safely
        elections.forEach(election -> {
            if (election.getVoters() != null) {
                election.getVoters().size(); // Force initialization
            }
        });
        return elections;
    }

    @Scheduled(cron = "0 * * * * *") // Still every minute but more precise timing
    @Transactional
    @Async("electionTaskExecutor")
    public void updateElectionStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // Only fetch elections that might need status changes
        List<Election> elections = electionRepository.findElectionsNeedingStatusUpdate(
                now.minusDays(1), // Check elections starting/ending soon
                now.plusDays(1));

        // Batch update
        // set the active status based on current time
        // If the election is active, it should be active if the current time is between
        // startDate and endDate
        List<Election> toUpdate = elections.stream()
                .filter(election -> {
                    boolean shouldBeActive = !now.isBefore(election.getStartDate())
                            && now.isBefore(election.getEndDate());
                    return election.isActive() != shouldBeActive;
                })
                .peek(election -> election.setActive(!election.isActive()))
                .collect(Collectors.toList());

        if (!toUpdate.isEmpty()) {
            electionRepository.saveAll(toUpdate);
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Top of every hour
    @Transactional
    @Async("electionTaskExecutor")
    public void initializeMissingKeys() {
        // Process larger batches but less frequently
        int processed = 0;
        Page<Election> page;

        do {
            page = electionRepository.findByPublicAccessKeyIsNull(
                    PageRequest.of(0, 500, Sort.by(Sort.Direction.ASC, "id")));

            List<Election> toSave = page.getContent();

            if (!toSave.isEmpty()) {
                toSave.forEach(election -> {
                    // Manually generate the key
                    if (election.getPublicAccessKey() == null || election.getPublicAccessKey().isEmpty()) {
                        election.setSystemUpdate(LocalDateTime.now());
                    }
                });
                electionRepository.saveAll(toSave);
                processed += toSave.size();
            }
        } while (page.hasNext() && processed < 5000); // Limit to 5000 per run

        if (processed > 0) {
            log.info("Initialized {} election keys", processed);
        }
    }

}
