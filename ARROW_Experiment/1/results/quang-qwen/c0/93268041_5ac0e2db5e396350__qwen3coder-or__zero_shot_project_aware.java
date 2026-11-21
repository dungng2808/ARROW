package com.macasaet.fernet;

import static com.macasaet.fernet.Constants.signingKeyBytes;
import static com.macasaet.fernet.Constants.encryptionKeyBytes;
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
    public void testKeyConstructorWithSeparateKeys() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        
        assertArrayEquals(signingKey, key.getSigningKey());
        assertArrayEquals(encryptionKey, key.getEncryptionKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyConstructorWithNullSigningKey() {
        new Key(null, new byte[encryptionKeyBytes]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyConstructorWithWrongSigningKeyLength() {
        new Key(new byte[signingKeyBytes - 1], new byte[encryptionKeyBytes]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyConstructorWithNullEncryptionKey() {
        new Key(new byte[signingKeyBytes], null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyConstructorWithWrongEncryptionKeyLength() {
        new Key(new byte[signingKeyBytes], new byte[encryptionKeyBytes - 1]);
    }

    @Test
    public void testKeyConstructorWithConcatenatedKeys() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        byte[] concatenated = new byte[signingKeyBytes + encryptionKeyBytes];
        System.arraycopy(signingKey, 0, concatenated, 0, signingKeyBytes);
        System.arraycopy(encryptionKey, 0, concatenated, signingKeyBytes, encryptionKeyBytes);
        
        Key key = new Key(concatenated);
        
        assertArrayEquals(signingKey, key.getSigningKey());
        assertArrayEquals(encryptionKey, key.getEncryptionKey());
    }

    @Test
    public void testKeyConstructorWithString() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key originalKey = new Key(signingKey, encryptionKey);
        String serialized = originalKey.serialise();
        
        Key keyFromString = new Key(serialized);
        
        assertArrayEquals(originalKey.getSigningKey(), keyFromString.getSigningKey());
        assertArrayEquals(originalKey.getEncryptionKey(), keyFromString.getEncryptionKey());
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
        Key key1 = Key.generateKey(random);
        Key key2 = Key.generateKey(random);
        
        assertNotEquals(key1.getSigningKey(), key2.getSigningKey());
        assertNotEquals(key1.getEncryptionKey(), key2.getEncryptionKey());
    }

    @Test
    public void testEncryptAndDecrypt() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        byte[] payload = "Hello World".getBytes();
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        
        byte[] encrypted = key.encrypt(payload, iv);
        byte[] decrypted = key.decrypt(encrypted, iv);
        
        assertArrayEquals(payload, decrypted);
    }

    @Test
    public void testSign() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        byte[] payload = "Hello World".getBytes();
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        byte[] encrypted = key.encrypt(payload, iv);
        
        byte[] signature = key.sign((byte) 1, Instant.now(), iv, encrypted);
        
        assertTrue(signature.length > 0);
    }

    @Test
    public void testSerialise() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        String serialised = key.serialise();
        
        assertNotNull(serialised);
        assertTrue(serialised.length() > 0);
    }

    @Test
    public void testWriteTo() throws IOException {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        key.writeTo(baos);
        
        byte[] written = baos.toByteArray();
        assertEquals(signingKeyBytes + encryptionKeyBytes, written.length);
        assertArrayEquals(signingKey, Arrays.copyOf(written, signingKeyBytes));
        assertArrayEquals(encryptionKey, Arrays.copyOfRange(written, signingKeyBytes, written.length));
    }

    @Test
    public void testHashCode() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key1 = new Key(signingKey, encryptionKey);
        Key key2 = new Key(signingKey, encryptionKey);
        
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void testEquals() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key1 = new Key(signingKey, encryptionKey);
        Key key2 = new Key(signingKey, encryptionKey);
        Key key3 = new Key(new byte[signingKeyBytes], new byte[encryptionKeyBytes]);
        
        assertEquals(key1, key2);
        assertNotEquals(key1, key3);
        assertNotEquals(key1, new Object());
        assertEquals(key1, key1);
    }

    @Test
    public void testGetSigningKeySpec() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        java.security.Key signingKeySpec = key.getSigningKeySpec();
        
        assertNotNull(signingKeySpec);
        assertEquals("HmacSHA256", signingKeySpec.getAlgorithm());
    }

    @Test
    public void testGetEncryptionKeySpec() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        javax.crypto.spec.SecretKeySpec encryptionKeySpec = key.getEncryptionKeySpec();
        
        assertNotNull(encryptionKeySpec);
        assertEquals("AES", encryptionKeySpec.getAlgorithm());
    }

    @Test
    public void testGetSigningKey() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        byte[] retrievedSigningKey = key.getSigningKey();
        
        assertArrayEquals(signingKey, retrievedSigningKey);
        // Verify that modifying the returned array doesn't affect the internal state
        retrievedSigningKey[0] = (byte) 99;
        assertArrayEquals(signingKey, key.getSigningKey());
    }

    @Test
    public void testGetEncryptionKey() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        byte[] retrievedEncryptionKey = key.getEncryptionKey();
        
        assertArrayEquals(encryptionKey, retrievedEncryptionKey);
        // Verify that modifying the returned array doesn't affect the internal state
        retrievedEncryptionKey[0] = (byte) 99;
        assertArrayEquals(encryptionKey, key.getEncryptionKey());
    }

    @Test
    public void testGetTokenPrefixBytes() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        int prefixBytes = key.getTokenPrefixBytes();
        
        assertEquals(17, prefixBytes);
    }

    @Test
    public void testGetSigningAlgorithm() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        String signingAlgorithm = key.getSigningAlgorithm();
        
        assertEquals("HmacSHA256", signingAlgorithm);
    }

    @Test
    public void testGetEncryptionAlgorithm() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        String encryptionAlgorithm = key.getEncryptionAlgorithm();
        
        assertEquals("AES", encryptionAlgorithm);
    }

    @Test
    public void testGetEncoder() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        java.util.Base64.Encoder encoder = key.getEncoder();
        
        assertNotNull(encoder);
    }

    @Test
    public void testGetCipherTransformation() {
        byte[] signingKey = new byte[signingKeyBytes];
        byte[] encryptionKey = new byte[encryptionKeyBytes];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        
        Key key = new Key(signingKey, encryptionKey);
        String cipherTransformation = key.getCipherTransformation();
        
        assertEquals("AES/CBC/PKCS5Padding", cipherTransformation);
    }

    private void assertNotNull(Object obj) {
        if (obj == null) {
            fail("Object should not be null");
        }
    }
}
