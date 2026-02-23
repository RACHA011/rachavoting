package com.racha.rachavoting.services;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class EncryptionService {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    
    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    // @Value("${voting.encryption.key}")
    private final String base64Key = "idnxidK2Nd4YXZ7AmrXPsr9+o1NjI5/qkz07sqvLqCY=";
    
    public EncryptionService() {
        this.secureRandom = new SecureRandom();
        
        try {
            byte[] keyBytes = Base64.getDecoder().decode(this.base64Key);
            this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid encryption key format", e);
        }
    }
    
    /**
     * Encrypt user ID with AES-256-GCM
     * Returns: Base64(IV + EncryptedData + AuthTag)
     */
    public String encryptUserId(String userId) {
        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            
            // Encrypt
            byte[] plaintext = userId.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintext);
            
            // Combine IV + Ciphertext (includes auth tag)
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, 0, encryptedWithIv, GCM_IV_LENGTH, ciphertext.length);
            
            // Return Base64 encoded result
            return Base64.getEncoder().encodeToString(encryptedWithIv);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt user ID", e);
        }
    }
    
    /**
     * Decrypt user ID from AES-256-GCM
     * Input: Base64(IV + EncryptedData + AuthTag)
     */
    public String decryptUserId(String encryptedUserId) {
        try {
            // Decode from Base64
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedUserId);
            
            // Extract IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
            
            // Extract ciphertext
            byte[] ciphertext = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);
            
            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            
            // Decrypt and return
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt user ID - data may be corrupted", e);
        }
    }
    
    // /**
    //  * Generate a new 256-bit AES key (use this to generate your key for application.properties)
    //  */
    // public static String generateNewKey() {
    //     try {
    //         KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
    //         keyGenerator.init(256);
    //         SecretKey key = keyGenerator.generateKey();
    //         return Base64.getEncoder().encodeToString(key.getEncoded());
    //     } catch (NoSuchAlgorithmException e) {
    //         throw new RuntimeException("Failed to generate encryption key", e);
    //     }
    // }
}