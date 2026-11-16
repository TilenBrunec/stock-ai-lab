package com.example.stocklab.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Šifriranje občutljivih podatkov (OpenRouter ključev) z AES-256-GCM.
 * Rezultat je base64(IV + ciphertext), IV je nov in naključen ob vsakem šifriranju.
 */
@Service
public class CryptoService {

    private static final String ALGORITEM = "AES/GCM/NoPadding";
    private static final int IV_DOLZINA = 12;      // priporočena dolžina IV za GCM
    private static final int TAG_BITOV = 128;

    private final SecretKey kljuc;
    private final SecureRandom random = new SecureRandom();

    public CryptoService(@Value("${app.encryption-key:}") String base64Kljuc) {
        if (base64Kljuc == null || base64Kljuc.isBlank()) {
            throw new IllegalStateException(
                    "Manjka app.encryption-key (APP_ENCRYPTION_KEY) - 32 bajtov v base64, glej README");
        }
        byte[] bajti = Base64.getDecoder().decode(base64Kljuc.trim());
        if (bajti.length != 32) {
            throw new IllegalStateException("app.encryption-key mora biti 32 bajtov (AES-256), je " + bajti.length);
        }
        this.kljuc = new SecretKeySpec(bajti, "AES");
    }

    public String encrypt(String besedilo) {
        try {
            byte[] iv = new byte[IV_DOLZINA];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITEM);
            cipher.init(Cipher.ENCRYPT_MODE, kljuc, new GCMParameterSpec(TAG_BITOV, iv));
            byte[] sifrirano = cipher.doFinal(besedilo.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(
                    ByteBuffer.allocate(iv.length + sifrirano.length).put(iv).put(sifrirano).array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Šifriranje ni uspelo", e);
        }
    }

    public String decrypt(String base64) {
        try {
            byte[] vse = Base64.getDecoder().decode(base64);
            ByteBuffer buf = ByteBuffer.wrap(vse);
            byte[] iv = new byte[IV_DOLZINA];
            buf.get(iv);
            byte[] sifrirano = new byte[buf.remaining()];
            buf.get(sifrirano);

            Cipher cipher = Cipher.getInstance(ALGORITEM);
            cipher.init(Cipher.DECRYPT_MODE, kljuc, new GCMParameterSpec(TAG_BITOV, iv));
            return new String(cipher.doFinal(sifrirano), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Dešifriranje ni uspelo (napačen ključ ali pokvarjeni podatki)", e);
        }
    }
}
