package com.racha.rachavoting.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.racha.rachavoting.model.Logo;
import com.racha.rachavoting.util.logo.LogoStatus;

public interface LogoRepository extends JpaRepository<Logo, Long> {
    Logo findByUniqueId(String uniqueId);

    List<Logo> findByLogoTypeId(Long logoTypeId);

    List<Logo> findByStatusAndCreatedAtBefore(
            LogoStatus status,
            LocalDateTime cutoff);
}
