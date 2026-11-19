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
    public void serialiseAndConstructFromSerialisedKey() {
        byte[] signing = new byte[16];
        byte[] encryption = new byte[16];
        for (int i = 0; i < 16; i++) {
            signing[i] = (byte) i;
            encryption[i] = (byte) (i + 16);
        }

        Key key = new Key(signing, encryption);
        String serialised = key.serialise();

        assertEquals("AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=", serialised);
        assertEquals(key, new Key(serialised));
    }

    @Test
    public void constructsFromConcatenatedBytes() {
        byte[] concatenated = new byte[32];
        for (int i = 0; i < concatenated.length; i++) {
            concatenated[i] = (byte) i;
        }

        Key key = new Key(concatenated);
        assertEquals(new Key(Arrays.copyOfRange(concatenated, 0, 16),
                Arrays.copyOfRange(concatenated, 16, 32)), key);
    }

    @Test
    public void writesSigningThenEncryptionKey() throws IOException {
        byte[] signing = new byte[16];
        byte[] encryption = new byte[16];
        Arrays.fill(signing, (byte) 1);
        Arrays.fill(encryption, (byte) 2);
        Key key = new Key(signing, encryption);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        key.writeTo(output);

        byte[] expected = new byte[32];
        Arrays.fill(expected, 0, 16, (byte) 1);
        Arrays.fill(expected, 16, 32, (byte) 2);
        assertArrayEquals(expected, output.toByteArray());
    }

    @Test
    public void encryptsAndDecryptsPayload() {
        byte[] signing = new byte[16];
        byte[] encryption = new byte[16];
        Key key = new Key(signing, encryption);
        byte[] payload = new byte[] { 1, 2, 3, 4, 5 };
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);

        byte[] encrypted = key.encrypt(payload, iv);

        assertEquals(16, encrypted.length);
        assertArrayEquals(payload, key.decrypt(encrypted, iv));
    }

    @Test
    public void signsTokenComponentsDeterministically() {
        Key key = new Key(new byte[16], new byte[16]);
        byte[] signature = key.sign((byte) 0x80, Instant.ofEpochSecond(1),
                new IvParameterSpec(new byte[16]), new byte[] { 1, 2, 3 });

        assertNotNull(signature);
        assertEquals(32, signature.length);
        assertArrayEquals(signature, key.sign((byte) 0x80, Instant.ofEpochSecond(1),
                new IvParameterSpec(new byte[16]), new byte[] { 1, 2, 3 }));
    }

    @Test
    public void equalKeysHaveEqualHashCodes() {
        byte[] signing = new byte[16];
        byte[] encryption = new byte[16];
        Key first = new Key(signing, encryption);
        Key second = new Key(signing.clone(), encryption.clone());

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(first.equals(first));
    }

    @Test
    public void differentKeysAreNotEqual() {
        byte[] signing = new byte[16];
        byte[] encryption = new byte[16];
        encryption[0] = 1;
        Key first = new Key(new byte[16], new byte[16]);
        Key second = new Key(signing, encryption);

        assertFalse(first.equals(second));
        assertFalse(first.equals("not a key"));
        assertNotEquals(first, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsSigningKeyWithWrongLength() {
        new Key(new byte[15], new byte[16]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEncryptionKeyWithWrongLength() {
        new Key(new byte[16], new byte[15]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullSigningKey() {
        new Key(null, new byte[16]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullEncryptionKey() {
        new Key(new byte[16], null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsShortConcatenatedKey() {
        new Key(new byte[31]);
    }
}
