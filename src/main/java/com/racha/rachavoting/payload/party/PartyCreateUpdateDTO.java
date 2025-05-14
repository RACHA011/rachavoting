package com.racha.rachavoting.payload.party;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyCreateUpdateDTO {
    private String name;
    private String abbreviation;
    private String leader;
    private String slogan;
    private String description;
    private List<String> colors;
    private LocalDate foundingDate;
    private String logoUrl;
    private String website;
    private String headquarters;
    private String registrationNo;
    private String facebook;
    private String x;
    private String instagram;
    private String youtube;
}