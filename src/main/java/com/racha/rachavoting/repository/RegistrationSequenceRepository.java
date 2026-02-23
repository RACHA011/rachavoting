package com.racha.rachavoting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.racha.rachavoting.model.components.RegistrationSequence;

@Repository
public interface RegistrationSequenceRepository extends JpaRepository<RegistrationSequence, Long> {

    RegistrationSequence findByYearAndType(int year, String type);

}
