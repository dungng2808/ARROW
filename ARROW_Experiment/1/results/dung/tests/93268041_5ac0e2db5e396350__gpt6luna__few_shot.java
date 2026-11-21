package com.macasaet.fernet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import javax.crypto.spec.IvParameterSpec;
import org.junit.Test;

public class Key_ArrowTest {

    @Test
    public void serialiseAndParseKey() {
        byte[] signingKey = new byte[16];
        byte[] encryptionKey = new byte[16];
        for (int i = 0; i < 16; i++) {
            signingKey[i] = (byte) i;
            encryptionKey[i] = (byte) (i + 16);
        }

        Key key = new Key(signingKey, encryptionKey);
        Key parsed = new Key(key.serialise());

        assertEquals(key, parsed);
        assertEquals(key.serialise(), parsed.serialise());
    }

    @Test
    public void concatenatedKeySplitsSigningAndEncryptionBytes() {
        byte[] combined = new byte[32];
        for (int i = 0; i < combined.length; i++) {
            combined[i] = (byte) i;
        }

        Key key = new Key(combined);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            key.writeTo(output);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        assertArrayEquals(combined, output.toByteArray());
    }

    @Test
    public void rejectsInvalidSigningKeyLength() {
        try {
            new Key(new byte[15], new byte[16]);
            throw new AssertionError("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("Signing key must be 128 bits", expected.getMessage());
        }
    }

    @Test
    public void rejectsInvalidEncryptionKeyLength() {
        try {
            new Key(new byte[16], new byte[15]);
            throw new AssertionError("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("Encryption key must be 128 bits", expected.getMessage());
        }
    }

    @Test
    public void encryptsAndDecryptsPayload() {
        byte[] signingKey = new byte[16];
        byte[] encryptionKey = new byte[16];
        Arrays.fill(signingKey, (byte) 1);
        Arrays.fill(encryptionKey, (byte) 2);
        Key key = new Key(signingKey, encryptionKey);
        byte[] payload = new byte[] {1, 2, 3, 4, 5};
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);

        byte[] encrypted = key.encrypt(payload, iv);
        byte[] decrypted = key.decrypt(encrypted, iv);

        assertArrayEquals(payload, decrypted);
    }

    @Test
    public void signsTokenComponentsDeterministically() {
        byte[] signingKey = new byte[16];
        byte[] encryptionKey = new byte[16];
        Arrays.fill(signingKey, (byte) 3);
        Key key = new Key(signingKey, encryptionKey);
        byte[] cipherText = new byte[] {4, 5, 6};
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        Instant timestamp = Instant.ofEpochSecond(123456789L);

        byte[] first = key.sign((byte) 0x80, timestamp, iv, cipherText);
        byte[] second = key.sign((byte) 0x80, timestamp, iv, cipherText);

        assertArrayEquals(first, second);
        assertEquals(32, first.length);
    }

    @Test
    public void equalityAndHashCodeUseBothKeyComponents() {
        byte[] signingKey = new byte[16];
        byte[] encryptionKey = new byte[16];
        Key first = new Key(signingKey, encryptionKey);
        Key equal = new Key(signingKey.clone(), encryptionKey.clone());
        byte[] differentEncryptionKey = new byte[16];
        differentEncryptionKey[0] = 1;
        Key different = new Key(signingKey, differentEncryptionKey);

        assertTrue(first.equals(equal));
        assertEquals(first.hashCode(), equal.hashCode());
        assertFalse(first.equals(different));
        assertNotEquals(first, "not a key");
    }
}
