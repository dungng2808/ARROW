package com.macasaet.fernet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;

import javax.crypto.spec.IvParameterSpec;

import org.junit.Test;

public class Key_ArrowTest {

    @Test
    public void testKeyByteArrayConstructor() {
        byte[] signingKey = new byte[16];
        byte[] encryptionKey = new byte[16];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        
        assertArrayEquals(signingKey, key.getSigningKey());
        assertArrayEquals(encryptionKey, key.getEncryptionKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyByteArrayConstructorWithWrongSigningKeyLength() {
        byte[] signingKey = new byte[15]; // Wrong length
        byte[] encryptionKey = new byte[16];
        
        new Key(signingKey, encryptionKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyByteArrayConstructorWithWrongEncryptionKeyLength() {
        byte[] signingKey = new byte[16];
        byte[] encryptionKey = new byte[15]; // Wrong length
        
        new Key(signingKey, encryptionKey);
    }

    @Test
    public void testKeyByteArrayConstructorWithNullSigningKey() {
        byte[] signingKey = null;
        byte[] encryptionKey = new byte[16];
        
        try {
            new Key(signingKey, encryptionKey);
        } catch (IllegalArgumentException e) {
            assertEquals("Signing key must be 128 bits", e.getMessage());
        }
    }

    @Test
    public void testKeyByteArrayConstructorWithNullEncryptionKey() {
        byte[] signingKey = new byte[16];
        byte[] encryptionKey = null;
        
        try {
            new Key(signingKey, encryptionKey);
        } catch (IllegalArgumentException e) {
            assertEquals("Encryption key must be 128 bits", e.getMessage());
        }
    }

    @Test
    public void testKeyConcatenatedKeysConstructor() {
        byte[] concatenatedKeys = new byte[32];
        Arrays.fill(concatenatedKeys, (byte) 1);
        
        Key key = new Key(concatenatedKeys);
        
        byte[] expectedSigningKey = Arrays.copyOfRange(concatenatedKeys, 0, 16);
        byte[] expectedEncryptionKey = Arrays.copyOfRange(concatenatedKeys, 16, 32);
        
        assertArrayEquals(expectedSigningKey, key.getSigningKey());
        assertArrayEquals(expectedEncryptionKey, key.getEncryptionKey());
    }

    @Test
    public void testKeyStringConstructor() {
        // Using a valid base64 url encoded string
        String keyString = "AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyA=";
        Key key = new Key(keyString);
        
        // Verify that the key was created successfully
        assertNotNull(key.getSigningKey());
        assertNotNull(key.getEncryptionKey());
    }

    @Test
    public void testGenerateKey() {
        Key key = Key.generateKey();
        
        assertNotNull(key);
        assertNotNull(key.getSigningKey());
        assertNotNull(key.getEncryptionKey());
        assertEquals(16, key.getSigningKey().length);
        assertEquals(16, key.getEncryptionKey().length);
    }

    @Test
    public void testGenerateKeyWithSecureRandom() {
        SecureRandom random = new SecureRandom();
        Key key = Key.generateKey(random);
        
        assertNotNull(key);
        assertNotNull(key.getSigningKey());
        assertNotNull(key.getEncryptionKey());
        assertEquals(16, key.getSigningKey().length);
        assertEquals(16, key.getEncryptionKey().length);
    }

    @Test
    public void testEncryptAndDecrypt() {
        Key key = Key.generateKey();
        byte[] payload = "Hello World".getBytes();
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        
        byte[] encrypted = key.encrypt(payload, iv);
        byte[] decrypted = key.decrypt(encrypted, iv);
        
        assertArrayEquals(payload, decrypted);
    }

    @Test
    public void testSerialise() {
        Key key = Key.generateKey();
        String serialised = key.serialise();
        
        assertNotNull(serialised);
        assertTrue(serialised.length() > 0);
    }

    @Test
    public void testWriteTo() throws IOException {
        Key key = Key.generateKey();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        key.writeTo(baos);
        byte[] writtenBytes = baos.toByteArray();
        
        assertEquals(32, writtenBytes.length);
        assertArrayEquals(key.getSigningKey(), Arrays.copyOfRange(writtenBytes, 0, 16));
        assertArrayEquals(key.getEncryptionKey(), Arrays.copyOfRange(writtenBytes, 16, 32));
    }

    @Test
    public void testHashCode() {
        Key key1 = Key.generateKey();
        Key key2 = Key.generateKey();
        
        // Different keys should have different hash codes
        assertNotEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void testEquals() {
        Key key1 = Key.generateKey();
        Key key2 = Key.generateKey();
        Key key3 = new Key(key1.getSigningKey(), key1.getEncryptionKey());
        
        // Same keys should be equal
        assertEquals(key1, key3);
        // Different keys should not be equal
        assertNotEquals(key1, key2);
        // Key should equal itself
        assertEquals(key1, key1);
        // Key should not equal null
        assertNotEquals(key1, null);
        // Key should not equal other type
        assertNotEquals(key1, "not a key");
    }

    @Test
    public void testSign() {
        Key key = Key.generateKey();
        byte version = 1;
        Instant timestamp = Instant.now();
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        byte[] cipherText = "test".getBytes();
        
        byte[] signature = key.sign(version, timestamp, iv, cipherText);
        
        assertNotNull(signature);
        assertTrue(signature.length > 0);
    }

    @Test
    public void testGetSigningKeySpec() {
        Key key = Key.generateKey();
        java.security.Key signingKeySpec = key.getSigningKeySpec();
        
        assertNotNull(signingKeySpec);
        assertEquals("HmacSHA256", signingKeySpec.getAlgorithm());
    }

    @Test
    public void testGetEncryptionKeySpec() {
        Key key = Key.generateKey();
        javax.crypto.spec.SecretKeySpec encryptionKeySpec = key.getEncryptionKeySpec();
        
        assertNotNull(encryptionKeySpec);
        assertEquals("AES", encryptionKeySpec.getAlgorithm());
    }

    @Test
    public void testGetSigningKey() {
        Key key = Key.generateKey();
        byte[] signingKey = key.getSigningKey();
        
        assertNotNull(signingKey);
        assertEquals(16, signingKey.length);
    }

    @Test
    public void testGetEncryptionKey() {
        Key key = Key.generateKey();
        byte[] encryptionKey = key.getEncryptionKey();
        
        assertNotNull(encryptionKey);
        assertEquals(16, encryptionKey.length);
    }

    @Test
    public void testGetTokenPrefixBytes() {
        Key key = Key.generateKey();
        int prefixBytes = key.getTokenPrefixBytes();
        
        assertEquals(17, prefixBytes);
    }

    @Test
    public void testGetSigningAlgorithm() {
        Key key = Key.generateKey();
        String signingAlgorithm = key.getSigningAlgorithm();
        
        assertEquals("HmacSHA256", signingAlgorithm);
    }

    @Test
    public void testGetEncryptionAlgorithm() {
        Key key = Key.generateKey();
        String encryptionAlgorithm = key.getEncryptionAlgorithm();
        
        assertEquals("AES", encryptionAlgorithm);
    }

    @Test
    public void testGetEncoder() {
        Key key = Key.generateKey();
        java.util.Base64.Encoder encoder = key.getEncoder();
        
        assertNotNull(encoder);
    }

    @Test
    public void testGetCipherTransformation() {
        Key key = Key.generateKey();
        String cipherTransformation = key.getCipherTransformation();
        
        assertEquals("AES/CTR/NoPadding", cipherTransformation);
    }
}
