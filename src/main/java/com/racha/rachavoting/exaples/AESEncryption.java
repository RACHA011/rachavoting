package com.racha.rachavoting.exaples;

// import com.racha.rachavoting.services.RegisteredEmailService;

// public class Main {

//     public static void main(String[] args) {
//         RegisteredEmailService registeredEmailService = new RegisteredEmailService();
//         System.out.println(registeredEmailService.encryptEmail(""));
//         // registeredEmailService.decryptEmail();
//     }

// }

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.racha.rachavoting.services.EncryptionService;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESEncryption {
    private static final String ALGORITHM = "AES";

    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decrypted = cipher.doFinal(decodedBytes);
        return new String(decrypted);
    }

    public static void main(String[] args) throws Exception {
        // // Generate a secret key (store securely!)
        // KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        // keyGen.init(128); // Can use 192 or 256 for stronger encryption
        // SecretKey key = keyGen.generateKey();

        // String originalId = "123456";
        // String encryptedId = encrypt(originalId, key);
        // String decryptedId = decrypt(encryptedId, key);
        // String x = Base64.getEncoder().encodeToString(key.getEncoded());

        // System.out.println("Original: " + originalId);
        // System.out.println("Encrypted: " + encryptedId);
        // System.out.println("Decrypted: " + decryptedId);
        // System.out.println("secret key" + x);
        // System.out.println(" key " + new SecretKeySpec(Base64.getDecoder().decode(x),
        // "AES"));
        // System.out.println("Key Algorithm: " + key.getAlgorithm());
        // System.out.println("Key " +key);

        String key = generateNewKey();
        System.out.println("Your encryption key: " + key);
    }

    public static String generateNewKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256);
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }
}
