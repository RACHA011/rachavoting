package com.racha.rachavoting.payload.constants;

import lombok.Data;

@Data
public class TokenResponse {
    private String votingToken;
    private String electionId;
    private long expiresAt;
    private String message;

    public TokenResponse(String votingToken, String electionId, long expiresAt) {
        this.votingToken = votingToken;
        this.electionId = electionId;
        this.expiresAt = expiresAt;
        this.message = "Token valid for voting period";
    }
}
