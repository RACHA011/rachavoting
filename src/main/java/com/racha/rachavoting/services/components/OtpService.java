package com.racha.rachavoting.services.components;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.racha.rachavoting.services.EmailService;
import com.racha.rachavoting.util.Hasher;
import com.racha.rachavoting.util.email.EmailDetails;

@Service
public class OtpService {

    @Autowired
    private SecureRandom secureRandom;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${app.otp.expiration}")
    private long otpExpirationMinutes;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 65536;

    @Value("${app.encryption.secret}")
    private  String secret_key;

    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    public String generateOtp(String email) {
        try {
            String otp = generateSecureOTP(6);

            EmailDetails emailDetails = new EmailDetails(email, "Your OTP is: " + otp + "\n\n" +
                    "Please complete your registration by entering this OTP in the form.\n\n" +
                    "This OTP is valid for 5 minutes.", "Racha Voting OTP");
            if (emailService.sendSimpleEmail(emailDetails)) {

                String otpHash = Hasher.hash(otp);
                String sessionToken = createEncryptedSession(email);

                // Store OTP in Redis with email as key, expires in 5 minutes
                redisTemplate.opsForValue().set(
                        "session token:" + sessionToken,
                        otpHash,
                        Duration.ofMinutes(otpExpirationMinutes));
                return sessionToken;
            }

        } catch (java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            System.out.println("Failed to generate OTP" + e);

        }
        return "";
    }

    // update this method to work whith hashed otp
    public boolean validateOtp(String sessionToken, String otp) {
        String storedOtp = (String) redisTemplate.opsForValue().get("session token:" + sessionToken);
        try {
            if (Hasher.verify(otp, storedOtp)) {
                // OTP is valid, remove it from Redis
                redisTemplate.delete("session token:" + sessionToken);
                return true;
            }
            return false;
        } catch (java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            return false;
        }
    }

    /**
     * Creates an encrypted session token containing email and timestamp
     */
    public String createEncryptedSession(String email) {
        try {
            // Create session data
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("email", email);
            sessionData.put("timestamp", System.currentTimeMillis());
            sessionData.put("sessionId", UUID.randomUUID().toString());

            // Encrypt the session data
            String encryptedToken = encryptSession(sessionData);

            return encryptedToken;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create encrypted session", e);
        }
    }
  
    /**
     * Decrypts and validates the session token
     */
    public String validateAndExtractEmail(String sessionToken) {
        try {
            // Decrypt the token
            String decryptedData = decryptSession(sessionToken);

            // Parse JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> sessionData = mapper.readValue(decryptedData, Map.class);

            // Validate timestamp (5 minutes expiry)
            long timestamp = ((Number) sessionData.get("timestamp")).longValue();
            long currentTime = System.currentTimeMillis();
            long fiveMinutes = 5 * 60 * 1000; // 5 minutes in milliseconds

            if (currentTime - timestamp > fiveMinutes) {
                throw new RuntimeException("Session expired");
            }

            String tokenEmail = (String) sessionData.get("email");

            return tokenEmail;

        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired session token", e);
        }
    }

    private String encryptSession(Map<String, Object> sessionData) throws Exception {
        // Generate random salt for each encryption
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        // Generate random IV for each encryption
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Derive secret key using PBKDF2 with random salt
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        KeySpec spec = new PBEKeySpec(secret_key.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        // Convert session data to JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = mapper.writeValueAsString(sessionData);

        // Encrypt
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedData = cipher.doFinal(jsonData.getBytes(StandardCharsets.UTF_8));

        // Combine salt + IV + encrypted data
        byte[] combined = new byte[salt.length + iv.length + encryptedData.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(encryptedData, 0, combined, salt.length + iv.length, encryptedData.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    private String decryptSession(String encryptedToken) throws Exception {
        // Decode from Base64
        byte[] combined = Base64.getDecoder().decode(encryptedToken);

        // Extract salt (first 16 bytes)
        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(combined, 0, salt, 0, salt.length);

        // Extract IV (next 16 bytes)
        byte[] iv = new byte[16];
        System.arraycopy(combined, salt.length, iv, 0, iv.length);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Extract encrypted data (remaining bytes)
        byte[] encryptedData = new byte[combined.length - salt.length - iv.length];
        System.arraycopy(combined, salt.length + iv.length, encryptedData, 0, encryptedData.length);

        // Re-derive secret key using the stored salt
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        KeySpec spec = new PBEKeySpec(secret_key.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

        // Decrypt
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] decryptedData = cipher.doFinal(encryptedData);

        // Convert back to Map
        String jsonData = new String(decryptedData, StandardCharsets.UTF_8);

        return jsonData;
    }

    /**
     * Generates a cryptographically secure random OTP using nextBytes
     * This method provides even stronger randomness
     * 
     * @param length The desired length of the OTP
     * @return String representing the generated OTP
     */
    public String generateSecureOTP(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be positive");
        }

        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);

        StringBuilder otp = new StringBuilder(length);

        for (byte b : randomBytes) {
            // Convert byte to positive int and map to digit
            int digit = Math.abs(b) % 10;
            otp.append(digit);
        }

        return otp.toString();
    }

}