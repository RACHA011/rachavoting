package com.racha.rachavoting.payload.party;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new political party.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyCreateDTO {
    public Long id;
    // Basic Party Information
    private String name;
    private String abbreviation;
    private boolean status;
    private String leader; // Party Leader's Name
    private String slogan; // Party Slogan
    private String description; // Party Description
    private boolean isRegistered; // Whether the party is officially registered
    private LocalDate foundingDate; // Founding Date of the Party
    private String logoUrl; // URL for Party Logo (optional)
    private String website; // Official Website (optional)
    
    private String uniqueId;// this will be the date at which the the logo was saved

    private String registrationNo;
    private String headquarters;

    // Party Colors
    private List<String> colors; // List of colors associated with the party

    // Social Media Handles
    private String facebook; // Facebook handle or URL
    private String x; // X (formerly Twitter) handle or URL
    private String instagram; // Instagram handle or URL
    private String youtube; // YouTube channel or URL
}