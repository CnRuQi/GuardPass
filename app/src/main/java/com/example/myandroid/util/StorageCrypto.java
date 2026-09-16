package com.example.myandroid.util;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class StorageCrypto {

    private static final String TAG = "StorageCrypto";
    private static final String KEYSTORE_ALIAS = "password_storage_key";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";

    private static volatile SecretKey cachedKey;

    private static SecretKey getOrCreateKey() throws Exception {
        if (cachedKey != null) return cachedKey;

        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(KEYSTORE_ALIAS, null);
            cachedKey = entry.getSecretKey();
            return cachedKey;
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        keyGenerator.init(new KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build());
        cachedKey = keyGenerator.generateKey();
        return cachedKey;
    }

    public static String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) return "";
        try {
            return encryptRequired(plaintext);
        } catch (Exception e) {
            Log.e(TAG, "Encryption failed", e);
            return "";
        }
    }

    public static String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) return "";
        try {
            return decryptRequired(encrypted);
        } catch (Exception e) {
            Log.e(TAG, "Decryption failed", e);
            return "";
        }
    }

    public static String encryptRequired(String plaintext) throws GeneralSecurityException {
        if (plaintext == null || plaintext.isEmpty()) return "";

        SecretKey key;
        try {
            key = getOrCreateKey();
        } catch (Exception e) {
            throw new GeneralSecurityException("Unable to access Android Keystore", e);
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV();
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    public static String decryptRequired(String encrypted) throws GeneralSecurityException {
        if (encrypted == null || encrypted.isEmpty()) return "";

        SecretKey key;
        try {
            key = getOrCreateKey();
        } catch (Exception e) {
            throw new GeneralSecurityException("Unable to access Android Keystore", e);
        }

        byte[] combined;
        try {
            combined = Base64.decode(encrypted, Base64.NO_WRAP);
        } catch (IllegalArgumentException e) {
            throw new GeneralSecurityException("Invalid encrypted value", e);
        }
        if (combined.length < GCM_IV_LENGTH + 1) {
            throw new GeneralSecurityException("Encrypted value is too short");
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, combined, 0, GCM_IV_LENGTH);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] plaintext = cipher.doFinal(combined, GCM_IV_LENGTH, combined.length - GCM_IV_LENGTH);
        return new String(plaintext, StandardCharsets.UTF_8);
    }
}
