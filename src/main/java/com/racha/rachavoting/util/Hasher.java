package com.racha.rachavoting.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility class for securely hashing and verifying values.
 * Uses PBKDF2 with HMAC-SHA256 following OWASP recommendations.
 */
public class Hasher {
    // Configuration (OWASP-recommended)
    private static final int ITERATIONS = 600_000;
    private static final int SALT_LENGTH = 16; // 128-bit salt
    private static final int KEY_LENGTH = 256; // 256-bit hash
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final String PEPPER = "SFEMQjprULytnymhTprrKAC84cHqQXlvitLlPGWw7yg=";

    /**
     * Hashes a national ID with a randomly generated salt.
     * 
     * @param nationalId The national ID to hash
     * @return A string in the format "salt:hash" where both values are Base64
     *         encoded
     * @throws NoSuchAlgorithmException If the hashing algorithm is not available
     * @throws InvalidKeySpecException  If there's an issue with the key
     *                                  specification
     */
    public static String hash(String nationalId) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = generateSalt();
        byte[] hash = calculateHash(nationalId + PEPPER, salt);
        return encodeForStorage(salt, hash);
    }

    /**
     * Verifies if a provided national ID matches a previously stored hash.
     * 
     * @param nationalId The national ID to verify
     * @param storedHash The previously stored hash in "salt:hash" format
     * @return true if the national ID matches the hash, false otherwise
     * @throws NoSuchAlgorithmException If the hashing algorithm is not available
     * @throws InvalidKeySpecException  If there's an issue with the key
     *                                  specification
     */
    public static boolean verify(String nationalId, String storedHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] parts = storedHash.split(":");
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash = calculateHash(nationalId + PEPPER, salt);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    // --- Helper Methods ---

    /**
     * Generates a cryptographically secure random salt.
     * 
     * @return A byte array containing the salt
     */
    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Calculates the hash of a national ID with the provided salt.
     * 
     * @param nationalId The national ID to hash
     * @param salt       The salt to use
     * @return A byte array containing the hash
     * @throws NoSuchAlgorithmException If the hashing algorithm is not available
     * @throws InvalidKeySpecException  If there's an issue with the key
     *                                  specification
     */
    private static byte[] calculateHash(String nationalId, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(
                nationalId.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }

    /**
     * Encodes the salt and hash for storage.
     * 
     * @param salt The salt bytes
     * @param hash The hash bytes
     * @return A string in the format "salt:hash" where both values are Base64
     *         encoded
     */
    private static String encodeForStorage(byte[] salt, byte[] hash) {
        return Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(hash);
    }

    // private static String generateSecurePepper() {
    // SecureRandom random = new SecureRandom();
    // byte[] bytes = new byte[32]; // 256 bits
    // random.nextBytes(bytes);
    // return Base64.getEncoder().encodeToString(bytes);
    // }

}