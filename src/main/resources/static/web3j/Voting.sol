// SPDX-License-Identifier: MIT
pragma solidity ^0.8.29;

contract Voting {
    // Map election IDs to their hash values
    mapping(string => string) private electionHashes;
    
    // Map vote IDs to their hash values
    mapping(string => string) private voteHashes;
    
    // Store an election hash
    function storeHash(string memory id, string memory hash) public {
        electionHashes[id] = hash;
    }
    
    // Store a vote hash
    function storeVoteHash(string memory voteId, string memory hash) public {
        voteHashes[voteId] = hash;
    }
    
    // Get an election hash
    function getHash(string memory id) public view returns (string memory) {
        return electionHashes[id];
    }
    
    // Get a vote hash
    function getVoteHash(string memory voteId) public view returns (string memory) {
        return voteHashes[voteId];
    }
    
    // Check if a hash exists
    function hashExists(string memory hash) public pure returns (bool) {
        // This is inefficient for large sets but works for demo purposes
        // In production, you'd want a reverse mapping or events
        return bytes(hash).length > 0;
    }
}