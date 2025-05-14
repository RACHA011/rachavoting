package com.racha.rachavoting.util.party;

/**
 * Enum representing specific party positions in South African politics
 * Each position is linked to its category
 */
public enum PartyPosition {
    // Party Leadership Positions
    PRESIDENT("President", PartyPositionCategory.PARTY_LEADERSHIP),
    DEPUTY_PRESIDENT("Deputy President", PartyPositionCategory.PARTY_LEADERSHIP),
    CHAIRPERSON("Chairperson", PartyPositionCategory.PARTY_LEADERSHIP),
    SECRETARY_GENERAL("Secretary-General", PartyPositionCategory.PARTY_LEADERSHIP),
    DEPUTY_SECRETARY_GENERAL("Deputy Secretary-General", PartyPositionCategory.PARTY_LEADERSHIP),
    TREASURER("Treasurer", PartyPositionCategory.PARTY_LEADERSHIP),
    NATIONAL_SPOKESPERSON("National Spokesperson", PartyPositionCategory.PARTY_LEADERSHIP),

    // Parliamentary Positions
    CHIEF_WHIP("Chief Whip", PartyPositionCategory.PARLIAMENTARY),
    PARLIAMENTARY_LEADER("Parliamentary Leader", PartyPositionCategory.PARLIAMENTARY),
    SHADOW_MINISTER("Shadow Minister", PartyPositionCategory.PARLIAMENTARY),
    COMMITTEE_CHAIR("Committee Chair", PartyPositionCategory.PARLIAMENTARY),

    // Regional/Provincial Leadership
    PROVINCIAL_CHAIRPERSON("Provincial Chairperson", PartyPositionCategory.PROVINCIAL),
    PROVINCIAL_SECRETARY("Provincial Secretary", PartyPositionCategory.PROVINCIAL),
    REGIONAL_COORDINATOR("Regional Coordinator", PartyPositionCategory.PROVINCIAL),

    // Electoral Representation
    NATIONAL_ASSEMBLY_REP("National Assembly Representative", PartyPositionCategory.ELECTORAL_REPRESENTATION),
    NCOP_REP("National Council of Provinces Representative", PartyPositionCategory.ELECTORAL_REPRESENTATION),
    MPL("Member of Provincial Legislature", PartyPositionCategory.ELECTORAL_REPRESENTATION),
    MUNICIPAL_COUNCILOR("Municipal Councilor", PartyPositionCategory.ELECTORAL_REPRESENTATION),

    // Specialized Roles
    YOUTH_LEAGUE_LEADER("Youth League Leader", PartyPositionCategory.SPECIALIZED_ROLES),
    WOMENS_LEAGUE_LEADER("Women's League Leader", PartyPositionCategory.SPECIALIZED_ROLES),
    VETERANS_LEAGUE_LEADER("Veterans' League Leader", PartyPositionCategory.SPECIALIZED_ROLES),
    CAMPAIGN_MANAGER("Election Campaign Manager", PartyPositionCategory.SPECIALIZED_ROLES),
    POLICY_COORDINATOR("Policy Coordinator", PartyPositionCategory.SPECIALIZED_ROLES),
    CONSTITUTIONAL_AFFAIRS_OFFICER("Constitutional Affairs Officer", PartyPositionCategory.SPECIALIZED_ROLES),

    // Executive Committee Members
    NEC_MEMBER("National Executive Committee Member", PartyPositionCategory.EXECUTIVE_COMMITTEE),
    FEDERAL_EXECUTIVE_MEMBER("Federal Executive Member", PartyPositionCategory.EXECUTIVE_COMMITTEE),
    WORKING_COMMITTEE_MEMBER("National Working Committee Member", PartyPositionCategory.EXECUTIVE_COMMITTEE);

    private final String displayName;
    private final PartyPositionCategory category;

    PartyPosition(String displayName, PartyPositionCategory category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PartyPositionCategory getCategory() {
        return category;
    }

    /**
     * Find all positions belonging to a specific category
     * 
     * @param category the category to filter by
     * @return array of positions in that category
     */
    public static PartyPosition[] getPositionsByCategory(PartyPositionCategory category) {
        return java.util.Arrays.stream(values())
                .filter(position -> position.getCategory() == category)
                .toArray(PartyPosition[]::new);
    }
}