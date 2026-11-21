package com.macasaet.fernet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

import javax.crypto.spec.IvParameterSpec;

import org.junit.Test;

public class Key_ArrowTest {

    @Test
    public void constructsFromComponentsAndSerialises() {
        byte[] signing = new byte[16];
        byte[] encryption = new byte[16];
        Arrays.fill(signing, (byte) 1);
        Arrays.fill(encryption, (byte) 2);

        Key key = new Key(signing, encryption);
        byte[] expected = new byte[32];
        System.arraycopy(signing, 0, expected, 0, 16);
        System.arraycopy(encryption, 0, expected, 16, 16);

        assertEquals(java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(expected), key.serialise());
    }

    @Test
    public void constructsFromConcatenatedBytes() {
        byte[] bytes = new byte[32];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        Key key = new Key(bytes);
        assertEquals(java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes), key.serialise());
    }

    @Test
    public void constructsFromSerialisedString() {
        byte[] bytes = new byte[32];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (31 - i);
        }
        String encoded = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        assertEquals(encoded, new Key(encoded).serialise());
    }

    @Test
    public void rejectsInvalidSigningKeyLength() {
        try {
            new Key(new byte[15], new byte[16]);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void rejectsNullSigningKey() {
        try {
            new Key(null, new byte[16]);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void rejectsInvalidEncryptionKeyLength() {
        try {
            new Key(new byte[16], new byte[15]);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void rejectsNullEncryptionKey() {
        try {
            new Key(new byte[16], null);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void writesSigningThenEncryptionBytes() throws IOException {
        byte[] signing = new byte[16];
        byte[] encryption = new byte[16];
        Arrays.fill(signing, (byte) 3);
        Arrays.fill(encryption, (byte) 4);
        Key key = new Key(signing, encryption);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        key.writeTo(output);

        byte[] expected = new byte[32];
        System.arraycopy(signing, 0, expected, 0, 16);
        System.arraycopy(encryption, 0, expected, 16, 16);
        assertArrayEquals(expected, output.toByteArray());
    }

    @Test
    public void equalKeysHaveEqualHashCodes() {
        byte[] signing = new byte[16];
        byte[] encryption = new byte[16];
        Key first = new Key(signing, encryption);
        Key second = new Key(signing.clone(), encryption.clone());

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void keyIsEqualToItself() {
        Key key = new Key(new byte[16], new byte[16]);

        assertTrue(key.equals(key));
    }

    @Test
    public void keysWithDifferentSigningBytesAreNotEqual() {
        byte[] signing = new byte[16];
        byte[] differentSigning = new byte[16];
        differentSigning[0] = 1;

        assertFalse(new Key(signing, new byte[16]).equals(new Key(differentSigning, new byte[16])));
    }

    @Test
    public void keysWithDifferentEncryptionBytesAreNotEqual() {
        byte[] encryption = new byte[16];
        byte[] differentEncryption = new byte[16];
        differentEncryption[0] = 1;

        assertFalse(new Key(new byte[16], encryption).equals(new Key(new byte[16], differentEncryption)));
    }

    @Test
    public void keyIsNotEqualToNullOrAnotherType() {
        Key key = new Key(new byte[16], new byte[16]);

        assertFalse(key.equals(null));
        assertFalse(key.equals("key"));
    }

    @Test
    public void encryptsAndDecryptsPayload() {
        Key key = new Key(new byte[16], new byte[16]);
        byte[] payload = new byte[] { 1, 2, 3, 4, 5 };
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);

        byte[] encrypted = key.encrypt(payload, iv);

        assertNotNull(encrypted);
        assertArrayEquals(payload, key.decrypt(encrypted, iv));
    }

    @Test
    public void signsTokenComponents() {
        Key key = new Key(new byte[16], new byte[16]);
        byte[] signature = key.sign((byte) 0x80, Instant.ofEpochSecond(1L),
                new IvParameterSpec(new byte[16]), new byte[] { 1, 2, 3 });

        assertEquals(32, signature.length);
    }

    @Test
    public void generatedKeyCanBeSerialised() {
        Key key = Key.generateKey();

        assertEquals(43, key.serialise().length());
    }

    @Test
    public void generatedKeyFromSecureRandomCanBeSerialised() {
        Key key = Key.generateKey(new java.security.SecureRandom());

        assertEquals(43, key.serialise().length());
    }
}
