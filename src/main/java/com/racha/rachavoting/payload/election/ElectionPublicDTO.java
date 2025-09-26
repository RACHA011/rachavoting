package com.racha.rachavoting.payload.election;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectionPublicDTO {
    private String publicAccessKey;

    private String title;

    private String year;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime registrationDeadline;
    
    private LocalDateTime resultAnouncement;

    private boolean isActive;

    private String description;
}
