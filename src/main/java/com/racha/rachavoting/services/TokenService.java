package com.racha.rachavoting.services;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.racha.rachavoting.payload.constants.TokenResponse;
import com.racha.rachavoting.payload.constants.VotingToken;
import com.racha.rachavoting.payload.constants.VotingTokenValidation;
import com.racha.rachavoting.services.components.OtpService;

import io.micrometer.common.util.StringUtils;

@Service
public class TokenService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EncryptionService encryptionService;

    @Value("${app.otp.reg.expiration}")
    private long regExpirationMinutes;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN_PREFIX = "voting_token";
    private static final int TOKEN_LENGTH = 32;
    private static final long TOKEN_VALIDITY_MINUTES = 30; // 30 minutes

    /**
     * Generates a secure registration token using a combination of
     * the current timestamp and random bytes.
     * 
     * @return A secure registration token.
     */
    public String generateRegistrationToken() {
        try {
            SecureRandom secureRandom = new SecureRandom();

            // Combine timestamp + random bytes for uniqueness
            long timestamp = Instant.now().toEpochMilli();
            byte[] randomBytes = new byte[24];
            secureRandom.nextBytes(randomBytes);

            // Create combined data
            String combined = timestamp + ":" + Base64.getEncoder().encodeToString(randomBytes);

            // Hash it for consistent length and security
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes());

            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Token generation failed", e);
        }
    }

    /**
     * Validates a registration token by checking its existence in Redis and
     * extracting the associated email address.
     * 
     * <p>
     * This method performs the following operations:
     * <ol>
     * <li>Retrieves the stored session token from Redis using the provided
     * registration token</li>
     * <li>If found, validates the session token and extracts the encrypted
     * email</li>
     * <li>Returns the decrypted email address if all validations pass</li>
     * </ol>
     * 
     * @param registrationToken The registration token to validate (non-null)
     * @return The decrypted email address if the token is valid, or empty string
     *         if:
     *         <ul>
     *         <li>The token doesn't exist in Redis</li>
     *         <li>The session token is invalid/expired</li>
     *         <li>Any error occurs during validation</li>
     *         </ul>
     * @throws IllegalArgumentException if registrationToken is null or blank
     */
    public String validateRegistrationToken(String registrationToken) {
        // Validate input
        if (StringUtils.isBlank(registrationToken)) {
            throw new IllegalArgumentException("Registration token cannot be null or blank");
        }

        try {
            // Build Redis key
            String redisKey = "registration token:" + registrationToken;

            // Retrieve session token from Redis
            String sessionToken = (String) redisTemplate.opsForValue().get(redisKey);

            if (sessionToken != null) {
                // Validate session token and extract email
                return otpService.validateAndExtractEmail(sessionToken);
            }

            // log.debug("No session token found for registration token: {}",
            // registrationToken);
            return "";

        } catch (Exception e) {
            // log.error("Failed to validate registration token: {}", registrationToken, e);
            return "";
        }
    }

    /**
     * Generate a secure voting token for user and election
     */
    public TokenResponse generateVotingToken(String userId, String electionId) {
        try {
            // Generate cryptographically secure raw token
            String rawToken = generateSecureToken();

            // Create hash for storage (never store raw token)
            String tokenHash = createSHA256Hash(rawToken);

            String hashedUser = createSHA256Hash(userId);

            // check if token exist if so it is removed
            removeToken(hashedUser, electionId);

            // Create token data
            VotingToken tokenData = new VotingToken();
            tokenData.setUserHash(hashedUser);
            tokenData.setUserEncrypt(encryptionService.encryptUserId(userId));
            tokenData.setElectionId(electionId);
            tokenData.setIssuedAt(System.currentTimeMillis());
            tokenData.setExpiresAt(System.currentTimeMillis() + (TOKEN_VALIDITY_MINUTES * 60 * 1000));

            // Store in Redis with TTL
            String tokenJson = objectMapper.writeValueAsString(tokenData);
            redisTemplate.opsForValue().set(
                    TOKEN_PREFIX + ":" + tokenHash,
                    tokenJson,
                    TOKEN_VALIDITY_MINUTES,
                    TimeUnit.MINUTES);

            // Return response with raw token (only time it's exposed)
            return new TokenResponse(rawToken, electionId, tokenData.getExpiresAt());

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate voting token", e);
        }
    }

    /**
     * Validate voting token when user attempts to vote
     */
    public VotingTokenValidation validateVotingToken(String rawToken) {
        try {
            // Hash the received token for lookup
            String tokenHash = createSHA256Hash(rawToken);

            // Retrieve from Redis
            String tokenJson = redisTemplate.opsForValue().get(TOKEN_PREFIX + ":" + tokenHash) + "";

            if (tokenJson == null || tokenJson.isEmpty()) {
                throw new IllegalArgumentException("Invalid or expired token");
            }

            VotingToken tokenData = objectMapper.readValue(tokenJson, VotingToken.class);

            // Validate token hasn't been used
            if (tokenData.isVoteCast()) {
                throw new IllegalStateException("Vote already cast with this token");
            }

            // Validate expiration
            if (System.currentTimeMillis() > tokenData.getExpiresAt()) {
                redisTemplate.delete(TOKEN_PREFIX + ":" + tokenHash);
                throw new IllegalArgumentException("Token expired");
            }

            return new VotingTokenValidation(tokenHash, tokenData.getElectionId(), tokenData.getUserHash());

        } catch (Exception e) {
            throw new RuntimeException("Token validation failed", e);
        }
    }

    /**
     * Mark token as used after successful vote
     */
    public void markTokenAsUsed(String tokenHash) {
        try {
            String tokenJson = redisTemplate.opsForValue().get(TOKEN_PREFIX + ":" + tokenHash) + "";
            if (tokenJson != null) {
                VotingToken tokenData = objectMapper.readValue(tokenJson, VotingToken.class);
                tokenData.setVoteCast(true);
                tokenData.setVoteCastAt(System.currentTimeMillis());

                String updatedJson = objectMapper.writeValueAsString(tokenData);
                redisTemplate.opsForValue().set(TOKEN_PREFIX + ":" + tokenHash, updatedJson, TOKEN_VALIDITY_MINUTES,
                        TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark token as used", e);
        }
    }

    public VotingToken getTokenDetails(String tokenHash) {
        try {
            String tokenJson = redisTemplate.opsForValue().get(TOKEN_PREFIX + ":" + tokenHash) + "";
            if (tokenJson != null && !tokenJson.isEmpty()) {
                return objectMapper.readValue(tokenJson, VotingToken.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve token details", e);
        }
    }

    // ==================== private methods ==================== //

    private void removeToken(String userHash, String electionId) {
        // Retrieve from Redis
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(TOKEN_PREFIX);

            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                String tokenHash = (String) entry.getKey();
                String tokenJson = (String) entry.getValue();

                if (tokenJson != null && !tokenJson.isEmpty()) {
                    VotingToken tokenData = objectMapper.readValue(tokenJson, VotingToken.class);
                    // If condition matches → delete this record

                    if (tokenData.getUserHash().equals(userHash) && tokenData.getElectionId().equals(electionId)) {
                        redisTemplate.opsForHash().delete(TOKEN_PREFIX, tokenHash);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Token validation failed", e);
        }
    }

    /**
     * Generate cryptographically secure token
     */
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : tokenBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Create SHA-256 hash
     */
    private String createSHA256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); // same results everywere for the same input
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
            throw new RuntimeException("Failed to create hash", e);
        }
    }

    /**
     * Stores the registration token in Redis with an encrypted email session.
     * 
     * @param token        The registration token to be stored.
     * @param sessionToken The session token containing the user's email.
     * @return true if the token was successfully stored, false otherwise.
     */
    public boolean storeToken(String token, String sessionToken) {
        try {
            // get the email then create another session token with the email
            String encryptedEmail = otpService
                    .createEncryptedSession(otpService.validateAndExtractEmail(sessionToken));

            // Store the registration token in Redis with the encrypted email session
            redisTemplate.opsForValue().set(
                    "registration token:" + token,
                    encryptedEmail,
                    Duration.ofMinutes(regExpirationMinutes));
            return true;
        } catch (Exception e) {
            // Log the error or handle it as needed
            System.err.println("Error storing token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes the registration token from Redis.
     * 
     * @param token The registration token to be deleted.
     * @return true if the token was successfully deleted, false otherwise.
     */
    public boolean deleteToken(String token) {
        try {
            // Build Redis key
            String redisKey = "registration token:" + token;

            // Delete the token from Redis
            return redisTemplate.delete(redisKey);
        } catch (Exception e) {
            // Log the error or handle it as needed
            System.err.println("Error deleting token: " + e.getMessage());
            return false;
        }
    }

}
