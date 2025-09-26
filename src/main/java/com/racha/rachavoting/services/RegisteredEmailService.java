package com.racha.rachavoting.services;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racha.rachavoting.model.RegisteredEmail;
import com.racha.rachavoting.repository.RegisteredEmailRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegisteredEmailService {

    @Autowired
    private RegisteredEmailRepository registeredEmailRepository;

    @Value("${app.encryption.secret}")
    private String secretKey;

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;

    public RegisteredEmail save(RegisteredEmail registeredEmail) {
        return registeredEmailRepository.save(registeredEmail);
    }

    /**
     * Checks if an email is already registered.
     * 
     * @param email The email to check
     * @return true if the email is registered, false otherwise
     */
    @Transactional(readOnly = true)
    public RegisteredEmail getByEmail(String email) {
        return registeredEmailRepository.findByEncryptedEmail(email)
                .orElseThrow(null);
    }

    @Transactional(readOnly = true)
    public RegisteredEmail getByToken(String unsubscribeToken) {
        return registeredEmailRepository.findByUnsubscribeToken(unsubscribeToken)
                .orElseThrow(null);
    }

    /**
     * Retrieves and decrypts all registered email addresses from the database.
     * 
     * @return List of decrypted email addresses
     * @throws DataAccessException if there's an issue accessing the data
     * @throws CryptoException     if email decryption fails
     */
    @Transactional(readOnly = true)
    public List<String> getAllRegisteredEmails() {
        try {
            // Fetch all encrypted emails from the repository
            List<String> encryptedEmails = registeredEmailRepository.findAll()
                    .stream()
                    .map(RegisteredEmail::getEncryptedEmail)
                    .filter(Objects::nonNull) // Filter out null values
                    .toList();

            // Decrypt each email address
            return encryptedEmails.stream()
                    .map(this::decryptEmail)
                    .filter(decrypted -> !decrypted.isBlank()) // Filter out empty results
                    .toList();
        } catch (DataAccessException e) {
            log.error("Failed to retrieve registered emails from database", e);
            throw new DataAccessException("Could not access registered email data", e) {
            };
        } catch (Exception e) {
            log.error("Unexpected error during email retrieval and decryption", e);
            throw new CryptoException("Failed to decrypt email addresses", e);
        }
    }

    /**
     * Retrieves and decrypts a registered email by its ID.
     * 
     * @param id The ID of the registered email
     * @return The decrypted email address
     * @throws EmailNotFoundException   if email not found
     * @throws EmailDecryptionException if decryption fails
     */
    public String getDecryptedEmail(Long id) {
        RegisteredEmail registeredEmail = registeredEmailRepository.findById(id)
                .orElseThrow(() -> new EmailNotFoundException("Email not found with id: " + id));

        try {
            return decrypt(registeredEmail.getEncryptedEmail());
        } catch (Exception e) {
            throw new EmailDecryptionException("Failed to decrypt email with id: " + id, e);
        }
    }

    /**
     * Encrypts an email address and saves it to the repository.
     * 
     * @param email The plaintext email to encrypt
     * @return The saved RegisteredEmail object containing the encrypted email
     * @throws EmailEncryptionException if encryption fails
     */
    public String encryptEmail(String email) {
        try {
            return encrypt(email);
        } catch (Exception e) {
            throw new EmailEncryptionException("Failed to encrypt email: " + email, e);
        }
    }

    /**
     * Decrypts an encrypted email address.
     * 
     * @param encryptedEmail The Base64 encoded encrypted email
     * @return The decrypted email address
     * @throws EmailDecryptionException if decryption fails
     */
    public String decryptEmail(String encryptedEmail) {
        try {
            return decrypt(encryptedEmail);
        } catch (Exception e) {
            throw new EmailDecryptionException("Failed to decrypt email: " + encryptedEmail, e);
        }
    }

    /**
     * Encrypts an email address using AES-256 with PBKDF2 key derivation.
     * 
     * @param email The plaintext email to encrypt
     * @return Base64 encoded string containing salt + IV + encrypted data
     * @throws Exception if encryption fails
     */
    private String encrypt(String email) throws Exception {
        SecureRandom random = new SecureRandom();

        // Generate salt and IV
        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(salt);
        random.nextBytes(iv);

        // Derive secret key
        SecretKey secretKey = deriveKey(salt);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Encrypt
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(email.getBytes(StandardCharsets.UTF_8));

        // Combine components
        byte[] combined = new byte[salt.length + iv.length + encrypted.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(encrypted, 0, combined, salt.length + iv.length, encrypted.length);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
        // return Base64.getUrlEncoder()
        // .withoutPadding()
        // .encodeToString(combined)
        // .replaceAll("[^a-zA-Z0-9]", ""); // Extra safety to remove any
        // non-alphanumeric chars
    }

    /**
     * Decrypts an email address encrypted by the encrypt() method.
     * 
     * @param encryptedData Base64 encoded string containing salt + IV + encrypted
     *                      data
     * @return The decrypted email address
     * @throws Exception if decryption fails
     */
    private String decrypt(String encryptedData) throws Exception {
        try {
            // URL-safe decoding first
            byte[] combined = Base64.getUrlDecoder().decode(encryptedData);

            if (combined.length < SALT_LENGTH + IV_LENGTH) {
                throw new IllegalArgumentException("Invalid encrypted data length");
            }

            // Extract components
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[combined.length - SALT_LENGTH - IV_LENGTH];

            System.arraycopy(combined, 0, salt, 0, salt.length);
            System.arraycopy(combined, salt.length, iv, 0, iv.length);
            System.arraycopy(combined, salt.length + iv.length, encrypted, 0, encrypted.length);

            // Derive key
            SecretKey secretKey = deriveKey(salt);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // Decrypt
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("Base64 decoding failed for input: {}", encryptedData);
            throw new EmailDecryptionException("Invalid Base64 encoding", e);
        } catch (Exception e) {
            log.error("Decryption failed for input: {}", encryptedData);
            throw new EmailDecryptionException("Decryption process failed", e);
        }
    }

    /**
     * Derives a secret key using PBKDF2 key derivation function.
     * 
     * @param salt The salt to use for key derivation
     * @return The derived secret key
     * @throws Exception if key derivation fails
     */
    private SecretKey deriveKey(byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    // Custom exceptions would be defined here as inner classes or separate files
    public static class EmailEncryptionException extends RuntimeException {
        public EmailEncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class EmailDecryptionException extends RuntimeException {
        public EmailDecryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class EmailNotFoundException extends RuntimeException {
        public EmailNotFoundException(String message) {
            super(message);
        }
    }

    public static class CryptoException extends RuntimeException {
        public CryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}