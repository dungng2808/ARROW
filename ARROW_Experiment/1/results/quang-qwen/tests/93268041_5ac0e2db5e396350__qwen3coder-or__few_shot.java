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
        byte[] signingKey = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        byte[] encryptionKey = new byte[]{17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
        
        Key key = new Key(signingKey, encryptionKey);
        
        assertArrayEquals(signingKey, key.getSigningKey());
        assertArrayEquals(encryptionKey, key.getEncryptionKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyByteArrayConstructorWithInvalidSigningKey() {
        byte[] signingKey = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}; // 15 bytes
        byte[] encryptionKey = new byte[]{17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
        
        new Key(signingKey, encryptionKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyByteArrayConstructorWithInvalidEncryptionKey() {
        byte[] signingKey = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        byte[] encryptionKey = new byte[]{17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31}; // 15 bytes
        
        new Key(signingKey, encryptionKey);
    }

    @Test
    public void testKeyByteArrayConstructorWithConcatenatedKeys() {
        byte[] concatenatedKeys = new byte[]{
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, // signing key
            17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32 // encryption key
        };
        
        Key key = new Key(concatenatedKeys);
        
        byte[] expectedSigningKey = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        byte[] expectedEncryptionKey = new byte[]{17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32};
        
        assertArrayEquals(expectedSigningKey, key.getSigningKey());
        assertArrayEquals(expectedEncryptionKey, key.getEncryptionKey());
    }

    @Test
    public void testKeyStringConstructor() {
        String base64Key = "AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyAhIiMkJSYnKCkqKywtLi8wMTIzNDU2Nzg5Ojs8PT4/QEFCQ0RFRkdISUpLTE1OT1BRUlNUVVZXWFlaW1xdXl9gYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXp7fH1+f4CBgoOEhYaHiImKi4yNjo+QkZKTlJWWl5iZmpucnZ6foKGio6SlpqeoqaqrrK2ur7CxsrO0tba3uLm6u7y9vr/AwcLDxMXGx8jJysvMzc7P0NHS09TV1tfY2drb3N3e3+Dh4uPk5ebn6Onq6+zt7u/w8fLz9PX29/j5+vv8/f7/AA==";
        
        Key key = new Key(base64Key);
        
        assertNotNull(key.getSigningKey());
        assertNotNull(key.getEncryptionKey());
    }

    @Test
    public void testGenerateKey() {
        Key key1 = Key.generateKey();
        Key key2 = Key.generateKey();
        
        assertNotEquals(key1.getSigningKey(), key2.getSigningKey());
        assertNotEquals(key1.getEncryptionKey(), key2.getEncryptionKey());
    }

    @Test
    public void testGenerateKeyWithSecureRandom() {
        SecureRandom random = new SecureRandom();
        Key key = Key.generateKey(random);
        
        assertNotNull(key.getSigningKey());
        assertNotNull(key.getEncryptionKey());
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
}
