package com.racha.rachavoting.payload.admin;

import java.math.BigInteger;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ElectionViewDTO {

    private String title;

    private String year;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime registrationDeadline;

    private boolean isActive;

    private String description;

    private BigInteger registeredVoters;

    private BigInteger totalVotes;
}
