package com.racha.rachavoting.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.racha.rachavoting.model.Vote;
import com.racha.rachavoting.repository.VoteRepository;

@Service
public class VoteService {
    
    @Autowired
    private VoteRepository voteRepository;

    public Vote saveVote(Vote vote) {
        return voteRepository.save(vote);
    }

    public Vote getVoteById(Long id) {
        return voteRepository.findById(id).orElse(null);
    }
}
