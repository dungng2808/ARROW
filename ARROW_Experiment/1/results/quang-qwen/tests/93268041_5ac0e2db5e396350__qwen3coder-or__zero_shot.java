package com.macasaet.fernet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;

import javax.crypto.spec.IvParameterSpec;

import org.junit.Test;

public class Key_ArrowTest {

    @Test
    public void testConstructorWithSeparateKeys() {
        byte[] signingKey = new byte[16];
        byte[] encryptionKey = new byte[16];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        
        assertArrayEquals(signingKey, key.getSigningKey());
        assertArrayEquals(encryptionKey, key.getEncryptionKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullSigningKey() {
        new Key(null, new byte[16]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithWrongSigningKeyLength() {
        new Key(new byte[15], new byte[16]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullEncryptionKey() {
        new Key(new byte[16], null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithWrongEncryptionKeyLength() {
        new Key(new byte[16], new byte[15]);
    }

    @Test
    public void testConstructorWithConcatenatedKeys() {
        byte[] keys = new byte[32];
        Arrays.fill(keys, 0, 16, (byte) 1);
        Arrays.fill(keys, 16, 32, (byte) 2);
        
        Key key = new Key(keys);
        
        assertArrayEquals(Arrays.copyOfRange(keys, 0, 16), key.getSigningKey());
        assertArrayEquals(Arrays.copyOfRange(keys, 16, 32), key.getEncryptionKey());
    }

    @Test
    public void testConstructorWithString() {
        String base64String = "AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRobHB0eHyAhIiMkJSYnKCkqKywtLi8wMTIzNDU2Nzg5Ojs8PT4/QEFCQ0RFRkdISUpLTE1OT1BRUlNUVVZXWFlaW1xdXl9gYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXp7fH1+f4CBgoOEhYaHiImKi4yNjo+QkZKTlJWWl5iZmpucnZ6foKGio6SlpqeoqaqrrK2ur7CxsrO0tba3uLm6u7y9vr/AwcLDxMXGx8jJysvMzc7P0NHS09TV1tfY2drb3N3e3+Dh4uPk5ebn6Onq6+zt7u/w8fLz9PX29/j5+vv8/f7/AA==";
        
        Key key = new Key(base64String);
        
        assertEquals(16, key.getSigningKey().length);
        assertEquals(16, key.getEncryptionKey().length);
    }

    @Test
    public void testGenerateKey() {
        Key key = Key.generateKey();
        
        assertEquals(16, key.getSigningKey().length);
        assertEquals(16, key.getEncryptionKey().length);
    }

    @Test
    public void testGenerateKeyWithSecureRandom() {
        SecureRandom random = new SecureRandom();
        Key key = Key.generateKey(random);
        
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
    public void testSign() {
        Key key = Key.generateKey();
        byte version = 1;
        Instant timestamp = Instant.now();
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        byte[] cipherText = "test".getBytes();
        
        byte[] signature = key.sign(version, timestamp, iv, cipherText);
        
        assertEquals(32, signature.length); // HMAC-SHA256 produces 32-byte signatures
    }

    @Test
    public void testSerialise() {
        Key key = Key.generateKey();
        String serialised = key.serialise();
        
        assertTrue(serialised.length() > 0);
    }

    @Test
    public void testWriteTo() throws IOException {
        Key key = Key.generateKey();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        key.writeTo(baos);
        
        byte[] written = baos.toByteArray();
        assertEquals(32, written.length);
        assertArrayEquals(key.getSigningKey(), Arrays.copyOfRange(written, 0, 16));
        assertArrayEquals(key.getEncryptionKey(), Arrays.copyOfRange(written, 16, 32));
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
    public void testGetSigningKeySpec() {
        Key key = Key.generateKey();
        java.security.Key signingKeySpec = key.getSigningKeySpec();
        
        assertEquals("HmacSHA256", signingKeySpec.getAlgorithm());
    }

    @Test
    public void testGetEncryptionKeySpec() {
        Key key = Key.generateKey();
        javax.crypto.spec.SecretKeySpec encryptionKeySpec = key.getEncryptionKeySpec();
        
        assertEquals("AES", encryptionKeySpec.getAlgorithm());
    }

    @Test
    public void testGetSigningKey() {
        Key key = Key.generateKey();
        byte[] signingKey = key.getSigningKey();
        
        assertEquals(16, signingKey.length);
    }

    @Test
    public void testGetEncryptionKey() {
        Key key = Key.generateKey();
        byte[] encryptionKey = key.getEncryptionKey();
        
        assertEquals(16, encryptionKey.length);
    }

    @Test
    public void testGetTokenPrefixBytes() {
        Key key = Key.generateKey();
        int prefixBytes = key.getTokenPrefixBytes();
        
        assertEquals(25, prefixBytes);
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
