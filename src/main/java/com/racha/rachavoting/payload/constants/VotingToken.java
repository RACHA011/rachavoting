package com.racha.rachavoting.payload.constants;

import lombok.Data;

@Data
public class VotingToken {
    private String userHash;
    private String userEncrypt;
    private String electionId;
    private long issuedAt;
    private long expiresAt;
    private boolean voteCast;
    private String tokenType;
    private Long voteCastAt;

    public VotingToken() {
        this.tokenType = "VOTING_TOKEN";
        this.voteCast = false;
    }
}
