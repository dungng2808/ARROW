package com.palantir.crypto2.keys.serialization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.palantir.crypto2.keys.KeyMaterial;
import java.security.InvalidKeyException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Test;

public class KeyMaterials_ArrowTest {

    @Test
    public void generateKeyReturnsAesKeyOfRequestedSize() {
        SecretKey key = KeyMaterials.generateKey("AES", 128);

        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
        assertEquals(16, key.getEncoded().length);
    }

    @Test
    public void generateIvReturnsRequestedLength() {
        byte[] iv = KeyMaterials.generateIv(12);

        assertEquals(12, iv.length);
    }

    @Test
    public void generateIvAllowsZeroLength() {
        byte[] iv = KeyMaterials.generateIv(0);

        assertArrayEquals(new byte[0], iv);
    }

    @Test
    public void generateKeyMaterialContainsRequestedKeyAndIvSizes() {
        KeyMaterial material = KeyMaterials.generateKeyMaterial("AES", 128, 12);

        assertNotNull(material);
        assertEquals(16, material.getSecretKey().getEncoded().length);
        assertEquals(12, material.getIv().length);
    }

    @Test
    public void fromCreatesMaterialWithProvidedKeyAndIv() {
        byte[] encodedKey = new byte[] {1, 2, 3, 4};
        byte[] iv = new byte[] {5, 6, 7};

        KeyMaterial material = KeyMaterials.from("AES", encodedKey, iv);

        assertArrayEquals(encodedKey, material.getSecretKey().getEncoded());
        assertEquals("AES", material.getSecretKey().getAlgorithm());
        assertArrayEquals(iv, material.getIv());
    }

    @Test
    public void getSafeKeyLengthReturnsRequestedLengthWhenAllowed() throws InvalidKeyException {
        assertEquals(128, KeyMaterials.getSafeKeyLength("AES", 128));
    }

    @Test
    public void getSafeKeyLengthReturnsZeroForZeroRequest() throws InvalidKeyException {
        assertEquals(0, KeyMaterials.getSafeKeyLength("AES", 0));
    }

    @Test
    public void getSafeKeyLengthRejectsUnsupportedAlgorithm() {
        try {
            KeyMaterials.getSafeKeyLength("not-a-real-algorithm", 128);
        } catch (RuntimeException exception) {
            assertNotNull(exception);
            return;
        } catch (InvalidKeyException exception) {
            assertNotNull(exception);
            return;
        }
        throw new AssertionError("Expected an exception for an unsupported algorithm");
    }

    @Test
    public void symmetricWrapAndUnwrapRoundTrip() {
        byte[] encodedKey = new byte[16];
        byte[] iv = new byte[] {1, 2, 3, 4};
        KeyMaterial original = KeyMaterials.from("AES", encodedKey, iv);
        SecretKey wrappingKey = new SecretKeySpec(new byte[16], "AES");

        byte[] wrapped = KeyMaterials.symmetricWrap(original, wrappingKey);
        KeyMaterial unwrapped = KeyMaterials.symmetricUnwrap(wrapped, wrappingKey);

        assertArrayEquals(encodedKey, unwrapped.getSecretKey().getEncoded());
        assertArrayEquals(iv, unwrapped.getIv());
    }

    @Test
    public void symmetricUnwrapRejectsEmptyInput() {
        SecretKey wrappingKey = new SecretKeySpec(new byte[16], "AES");

        try {
            KeyMaterials.symmetricUnwrap(new byte[0], wrappingKey);
        } catch (RuntimeException exception) {
            assertNotNull(exception);
            return;
        }
        throw new AssertionError("Expected an exception for an empty input");
    }

    @Test
    public void symmetricUnwrapRejectsUnknownVersion() {
        SecretKey wrappingKey = new SecretKeySpec(new byte[16], "AES");

        try {
            KeyMaterials.symmetricUnwrap(new byte[] {(byte) 127}, wrappingKey);
        } catch (RuntimeException exception) {
            assertNotNull(exception);
            return;
        }
        throw new AssertionError("Expected an exception for an unknown version");
    }
}
