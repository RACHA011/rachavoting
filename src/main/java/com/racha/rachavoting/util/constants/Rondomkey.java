package com.racha.rachavoting.util.constants;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rondomkey {
    private static final int SALT_LENGTH = 16; // 128 bits
    private static final int HASH_LENGTH = 32; // 256 bits
    private static final int ITERATIONS = 100000; // 100,000 iterations
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private final String lowerCaseLetter = "abcdefghijklmnopqrstuvwxyz";
    private final String upperCaseLetter = lowerCaseLetter.toUpperCase();
    private final String digits = "0123456789";
    private final SecureRandom random = new SecureRandom();
    private List<String> constants = new ArrayList<String>();

    private String rondomId;
    private String hashedId;

    public Rondomkey() {
        // we will use this constants to generate rondom numubers of specific length
        constants.add(lowerCaseLetter);
        constants.add(upperCaseLetter);
        constants.add(digits);
    }

    public Rondomkey(String rondomId, String secretKey) {
        this.rondomId = rondomId;
        this.hashedId = secretKey;

        constants.add(lowerCaseLetter);
        constants.add(upperCaseLetter);
        constants.add(digits);
    }

    public Rondomkey generateRondomkey() {
        String id = generateId(6);
        String hashedId = hashId(id);
        return new Rondomkey(id, hashedId);
    }

    private char getRandomChar(String charSet) {
        int temp = random.nextInt(charSet.length());
        return charSet.charAt(temp);
    }

    private String generateId(int length) {
        StringBuilder id = new StringBuilder();
        List<Character> idChars = new ArrayList<Character>();

        // first make sure that the id has the required chars
        idChars.add(getRandomChar(constants.get(0)));
        idChars.add(getRandomChar(constants.get(1)));
        idChars.add(getRandomChar(constants.get(2)));

        for (int i = 3; i < length; i++) {
            // get any element form the constants and use it to generate the next char
            idChars.add(getRandomChar(constants.get(random.nextInt(constants.size()))));
        }

        Collections.shuffle(idChars, random);

        for (Character c : idChars) {
            id.append(c);
        }

        return id.toString();
    }


    /**
     * Hash a random ID with salt and iterations
     * 
     * @param id The random ID to hash
     * @return Hashed string in format: iterations:salt:hash
     */
    public String hashId(String id) {
        try {
            // Generate random salt
            byte[] salt = generateSalt();

            // Create PBE key specification
            PBEKeySpec spec = new PBEKeySpec(
                    id.toCharArray(),
                    salt,
                    ITERATIONS,
                    HASH_LENGTH * 8);

            // Generate hash
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            // Encode components
            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            String encodedHash = Base64.getEncoder().encodeToString(hash);

            // Return format: iterations:salt:hash
            return ITERATIONS + ":" + encodedSalt + ":" + encodedHash;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing ID", e);
        }
    }

    /**
     * Verify if a plain text ID matches the stored hash
     * 
     * @param id         The plain text ID to verify
     * @param storedHash The stored hash in format: iterations:salt:hash
     * @return true if verification succeeds, false otherwise
     */
    public boolean verify(String id, String storedHash) {
        try {
            // Split the stored hash into components
            String[] parts = storedHash.split(":");
            if (parts.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

            // Hash the provided ID with the same parameters
            PBEKeySpec spec = new PBEKeySpec(
                    id.toCharArray(),
                    salt,
                    iterations,
                    expectedHash.length * 8);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = factory.generateSecret(spec).getEncoded();

            // Compare hashes in constant time to prevent timing attacks
            return constantTimeEquals(expectedHash, testHash);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generate a cryptographically secure random salt
     */
    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Compare two byte arrays in constant time to prevent timing attacks
     */
    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

}
