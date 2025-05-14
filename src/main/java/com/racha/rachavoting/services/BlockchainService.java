package com.racha.rachavoting.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true")
public class BlockchainService {
    // private final Web3j web3j;
    // private final Credentials credentials;
    // // private VotingContract votingContract;

    // @Value("${blockchain.contract-address:#{null}}")
    // private String contractAddress;

    // public BlockchainService(
    //         @Value("${blockchain.rpc-url}") String rpcUrl,
    //         @Value("${blockchain.private-key}") String privateKey) {
    //     this.web3j = Web3j.build(new HttpService(rpcUrl));
    //     this.credentials = Credentials.create(privateKey);
    // }

    // @PostConstruct
    // public void init() throws Exception {
    //     if (contractAddress != null && !contractAddress.isEmpty()) {
    //         this.votingContract = VotingContract.load(
    //                 contractAddress,
    //                 web3j,
    //                 credentials,
    //                 new DefaultGasProvider());
    //         System.out.println("Loaded existing contract at: " + contractAddress);
    //     } else {
    //         deployNewContract();
    //     }
    // }

    // private void deployNewContract() throws Exception {
    //     try {
    //         BigInteger gasLimit = BigInteger.valueOf(7_000_000L); // Adjust as needed
    //         BigInteger gasPrice = new BigInteger("2000000000");
    
    //         ContractGasProvider customGasProvider = new StaticGasProvider(gasPrice, gasLimit);
    
    //         this.votingContract = VotingContract.deploy(
    //                 web3j,
    //                 credentials,
    //                 customGasProvider).send();
    
    //         this.contractAddress = votingContract.getContractAddress();
    //         System.out.println("Deployed new contract at: " + contractAddress);
    //     } catch (Exception e) {
    //         throw new RuntimeException("Failed to deploy new contract: " + e.getMessage(), e);
    //     }
    // }

    // public void setContractAddress(String contractAddress) {
    //     this.contractAddress = contractAddress;
    //     loadExistingContract();
    // }

    // private void loadExistingContract() {
    //     System.out.println("Loading existing voting contract at address: " + contractAddress);
    //     this.votingContract = VotingContract.load(
    //             contractAddress,
    //             web3j,
    //             credentials,
    //             new DefaultGasProvider());
    // }

    // public String generateElectionHash(Election election) {
    //     try {
    //         // Format election data into a string
    //         DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    //         String electionData = election.getId() + "|" +
    //                 election.getTitle() + "|" +
    //                 election.getYear() + "|" +
    //                 election.getStartDate().format(formatter) + "|" +
    //                 election.getEndDate().format(formatter) + "|" +
    //                 election.getRegistrationDeadline().format(formatter) + "|" +
    //                 election.getCreatedBy() + "|" +
    //                 election.getCreatedAt().format(formatter);

    //         // Hash the election data with SHA-256
    //         MessageDigest digest = MessageDigest.getInstance("SHA-256");
    //         byte[] hash = digest.digest(electionData.getBytes(StandardCharsets.UTF_8));

    //         // Convert to hex string
    //         StringBuilder hexString = new StringBuilder();
    //         for (byte b : hash) {
    //             String hex = Integer.toHexString(0xff & b);
    //             if (hex.length() == 1)
    //                 hexString.append('0');
    //             hexString.append(hex);
    //         }
    //         return hexString.toString();
    //     } catch (NoSuchAlgorithmException e) {
    //         throw new RuntimeException("Error generating election hash", e);
    //     }
    // }

    // public String storeElectionHash(Election election) {
    //     try {
    //         if (votingContract == null) {
    //             throw new RuntimeException("Contract not initialized. Call init() method first.");
    //         }

    //         String electionHash = generateElectionHash(election);
    //         String electionId = "election_" + election.getId();

    //         // Store hash on blockchain
    //         TransactionReceipt receipt = votingContract.storeHash(electionId, electionHash).send();
    //         System.out.println("Election hash stored on blockchain. Transaction ID: " + receipt.getTransactionHash());

    //         return receipt.getTransactionHash();
    //     } catch (Exception e) {
    //         throw new RuntimeException("Failed to store election hash on blockchain: " + e.getMessage(), e);
    //     }
    // }

    // public String verifyElectionHash(Long electionId, String providedHash) {
    //     try {
    //         String electionIdStr = "election_" + electionId;
    //         String storedHash = votingContract.getHash(electionIdStr).send();

    //         if (storedHash.isEmpty()) {
    //             return "No hash found for this election";
    //         }

    //         if (storedHash.equals(providedHash)) {
    //             return "Hash verified successfully";
    //         } else {
    //             return "Hash verification failed: stored hash does not match provided hash";
    //         }
    //     } catch (Exception e) {
    //         throw new RuntimeException("Failed to verify election hash: " + e.getMessage(), e);
    //     }
    // }

}