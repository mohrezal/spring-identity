package com.github.mohrezal.identity.shared.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HashService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generateSecureToken(int byteLength) {
        if (byteLength <= 0) {
            throw new IllegalArgumentException("Byte length must be greater than zero");
        }

        var bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String sha256(String input) {
        byte[] hash = sha256Bytes(input);

        StringBuilder hexString = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String sha256Base64UrlEncoded(String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256Bytes(input));
    }

    public byte[] sha256Bytes(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
