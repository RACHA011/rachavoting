// package com.racha.rachavoting.blockchain;

// import java.io.IOException;
// import java.io.InputStream;
// import java.nio.charset.StandardCharsets;
// import java.util.Arrays;
// import java.util.Collections;

// import org.springframework.util.StreamUtils;
// import org.web3j.abi.TypeReference;
// import org.web3j.abi.datatypes.Bool;
// import org.web3j.abi.datatypes.Function;
// import org.web3j.abi.datatypes.Utf8String;
// import org.web3j.crypto.Credentials;
// import org.web3j.protocol.Web3j;
// import org.web3j.protocol.core.RemoteCall;
// import org.web3j.protocol.core.RemoteFunctionCall;
// import org.web3j.protocol.core.methods.response.TransactionReceipt;
// import org.web3j.tx.Contract;
// import org.web3j.tx.gas.ContractGasProvider;

// /**
//  * Web3j wrapper for the Voting smart contract.
//  * Loads contract binary and ABI from external resources.
//  */
// public class VotingContract extends Contract {
    
//     // Contract function names
//     public static final String FUNC_GETHASH = "getHash";
//     public static final String FUNC_GETVOTEHASH = "getVoteHash";
//     public static final String FUNC_HASHEXISTS = "hashExists";
//     public static final String FUNC_STOREHASH = "storeHash";
//     public static final String FUNC_STOREVOTEHASH = "storeVoteHash";

//     // Resource paths
//     private static final String ABI_PATH = "/contracts/Voting.abi";
//     private static final String BINARY_PATH = "/contracts/Voting.bin";
    
//     // Load contract binary and ABI from resources
//     private static final String BINARY = loadContractBinary();
//     private static final String ABI = loadContractABI();

//     protected VotingContract(String contractAddress, 
//                            Web3j web3j, 
//                            Credentials credentials,
//                            ContractGasProvider contractGasProvider) {
//         super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
//     }

//     /* ========== Contract Functions ========== */
    
//     public RemoteFunctionCall<String> getHash(String id) {
//         final Function function = new Function(
//             FUNC_GETHASH,
//             Arrays.asList(new Utf8String(id)),
//             Arrays.asList(new TypeReference<Utf8String>() {})
//         );
//         return executeRemoteCallSingleValueReturn(function, String.class);
//     }

//     public RemoteFunctionCall<String> getVoteHash(String voteId) {
//         final Function function = new Function(
//             FUNC_GETVOTEHASH,
//             Arrays.asList(new Utf8String(voteId)),
//             Arrays.asList(new TypeReference<Utf8String>() {})
//         );
//         return executeRemoteCallSingleValueReturn(function, String.class);
//     }

//     public RemoteFunctionCall<Boolean> hashExists(String hash) {
//         final Function function = new Function(
//             FUNC_HASHEXISTS,
//             Arrays.asList(new Utf8String(hash)),
//             Arrays.asList(new TypeReference<Bool>() {})
//         );
//         return executeRemoteCallSingleValueReturn(function, Boolean.class);
//     }

//     public RemoteFunctionCall<TransactionReceipt> storeHash(String id, String hash) {
//         final Function function = new Function(
//             FUNC_STOREHASH,
//             Arrays.asList(new Utf8String(id), new Utf8String(hash)),
//             Collections.emptyList()
//         );
//         return executeRemoteCallTransaction(function);
//     }

//     public RemoteFunctionCall<TransactionReceipt> storeVoteHash(String voteId, String hash) {
//         final Function function = new Function(
//             FUNC_STOREVOTEHASH,
//             Arrays.asList(new Utf8String(voteId), new Utf8String(hash)),
//             Collections.emptyList()
//         );
//         return executeRemoteCallTransaction(function);
//     }

//     /* ========== Contract Loading ========== */
    
//     public static VotingContract load(String contractAddress, 
//                                     Web3j web3j, 
//                                     Credentials credentials,
//                                     ContractGasProvider contractGasProvider) {
//         return new VotingContract(contractAddress, web3j, credentials, contractGasProvider);
//     }

//     public static RemoteCall<VotingContract> deploy(Web3j web3j, 
//                                                   Credentials credentials,
//                                                   ContractGasProvider contractGasProvider) {
//         return deployRemoteCall(
//             VotingContract.class, 
//             web3j, 
//             credentials, 
//             contractGasProvider, 
//             BINARY, 
//             ABI
//         );
//     }

//     /* ========== Helper Methods ========== */
    
//     private static String loadContractBinary() {
//         try (InputStream is = VotingContract.class.getResourceAsStream(BINARY_PATH)) {
//             if (is == null) {
//                 throw new IllegalStateException("Contract binary file not found at: " + BINARY_PATH);
//             }
//             return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
//         } catch (IOException e) {
//             throw new IllegalStateException("Failed to load contract binary", e);
//         }
//     }

//     private static String loadContractABI() {
//         try (InputStream is = VotingContract.class.getResourceAsStream(ABI_PATH)) {
//             if (is == null) {
//                 throw new IllegalStateException("Contract ABI file not found at: " + ABI_PATH);
//             }
//             return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
//         } catch (IOException e) {
//             throw new IllegalStateException("Failed to load contract ABI", e);
//         }
//     }
// }