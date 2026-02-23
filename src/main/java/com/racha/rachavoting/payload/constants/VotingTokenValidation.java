package com.racha.rachavoting.payload.constants;

import lombok.Data;

@Data
public class VotingTokenValidation {
    private String tokenHash;
    private String electionId;
    private String userHash;
    
    public VotingTokenValidation(String tokenHash, String electionId, String userHash) {
        this.tokenHash = tokenHash;
        this.electionId = electionId;
        this.userHash = userHash;
    }
}
