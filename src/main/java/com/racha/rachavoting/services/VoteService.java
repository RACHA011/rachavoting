package com.racha.rachavoting.services;

import java.security.MessageDigest;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racha.rachavoting.model.Election;
import com.racha.rachavoting.model.Vote;
import com.racha.rachavoting.model.Voter;
import com.racha.rachavoting.payload.constants.VotingToken;
import com.racha.rachavoting.payload.constants.VotingTokenValidation;
import com.racha.rachavoting.repository.VoteRepository;
import com.racha.rachavoting.util.constants.VoteResponse;

@Service
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private VoterService voterService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ElectionService electionService;

    @Autowired
    private EncryptionService encryptionService;

    public Vote saveVote(Vote vote) {
        return voteRepository.save(vote);
    }

    public Vote getVoteById(Long id) {
        return voteRepository.findById(id).orElse(null);
    }

    public Voter getByUniqueId(String uniqueId) {
        
        return null;
    }

    /**
     * Cast a vote using the voting token
     */
    @Transactional
    public VoteResponse castVote(String rawToken, String partyId) {
        try {
            // Validate the token
            VotingTokenValidation validation = tokenService.validateVotingToken(rawToken);

            // Get party details

            // Get voter details
            VotingToken tokenData = tokenService.getTokenDetails(validation.getTokenHash());
            if (tokenData == null) {
                throw new IllegalArgumentException("Invalid or used token");
            }

            if (tokenData.isVoteCast()) {
                throw new IllegalArgumentException("Token has already been used to cast a vote");
            }

            String userId = encryptionService.decryptUserId(tokenData.getUserEncrypt());

            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("Invalid user information in token");
            }

            Optional<Voter> optionalVoter = voterService.getByUniqueId(userId);

            if(!optionalVoter.isPresent()) {
                throw new IllegalArgumentException("Voter does not exist");
            }

            Voter voter = optionalVoter.get();

            // for now this will be invalis since voter and vote relationship is many to one
            if (voter.isHasVoted()) {
                throw new IllegalArgumentException("Voter has already cast a vote");
            }


            


            // Get election details
            Election election = electionService.findByPublicKey(validation.getElectionId());

            if (election == null) {
                throw new IllegalArgumentException("Election does not exist");
            }
            if (!election.isActive()) {
                throw new IllegalArgumentException("Election is not active ");
            }

            // TODO: make a separate post url or function to cast a vote for errors management inbetween process
            // Create vote record
            Vote vote = new Vote();
            vote.setElection(election);
            vote.setPartyId(partyId);
            vote.setTokenHash(validation.getTokenHash());
            // vote.setCastAt(System.currentTimeMillis());
            vote.setVoteHash(createVoteHash(validation.getElectionId(), partyId, validation.getTokenHash()));

            // Save vote to database
            vote = voteRepository.save(vote);

            // Mark token as used
            tokenService.markTokenAsUsed(validation.getTokenHash());

            return new VoteResponse(true, "Vote cast successfully");

        } catch (Exception e) {
            throw new RuntimeException("Vote casting failed: " + e.getMessage(), e);
        }
    }

    private String createVoteHash(String electionId, String partyId, String tokenHash) {
        // Create integrity hash for vote verification
        String input = electionId + partyId + tokenHash + System.currentTimeMillis();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create vote hash", e);
        }
    }
}
