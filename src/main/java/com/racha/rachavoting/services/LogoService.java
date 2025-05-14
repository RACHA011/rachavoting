package com.racha.rachavoting.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racha.rachavoting.model.Logo;
import com.racha.rachavoting.repository.LogoRepository;
import com.racha.rachavoting.util.logo.LogoStatus;

@Service
@Transactional(readOnly = true)
public class LogoService {

    @Autowired
    private LogoRepository logoRepository;

    @Value("${logo.temporary.expiration-hours:24}")
    private int expirationHours;

    public Logo save(Logo logo) {
        return logoRepository.save(logo);
    }

    public Logo findById(Long id) {
        return logoRepository.findById(id).orElse(null);
    }

    public Logo findByUniqueId(String uniqueId) {
        return logoRepository.findByUniqueId(uniqueId);
    }

    public List<Logo> findByLogoTypeId(Long logoTypeId) {
        return logoRepository.findByLogoTypeId(logoTypeId);
    }

    public void delete(Logo logo) {
        logoRepository.delete(logo);
    }

    @Scheduled(fixedRate = 3600000) // Runs every hour
    public void cleanupTemporaryLogos() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(expirationHours);

        List<Logo> orphanedLogos = logoRepository.findByStatusAndCreatedAtBefore(
                LogoStatus.TEMPORARY,
                cutoff);

        try {
            orphanedLogos.forEach(logo -> {
                // Delete the database record
                logoRepository.delete(logo);
            });
        } catch (Exception e) {
            System.out.println("Error deleting orphaned logos: " + e.getMessage());
        }

    }

}
