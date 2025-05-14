package com.racha.rachavoting.util.party;

/**
 * Enum representing the category of party positions in South African politics
 */
public enum PartyPositionCategory {
    PARTY_LEADERSHIP("Party Leadership Positions"),
    PARLIAMENTARY("Parliamentary Positions"),
    PROVINCIAL("Provincial Leadership"),
    ELECTORAL_REPRESENTATION("Electoral Representation"),
    SPECIALIZED_ROLES("Specialized Roles"),
    EXECUTIVE_COMMITTEE("Executive Committee Members");

    private final String displayName;

    PartyPositionCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
