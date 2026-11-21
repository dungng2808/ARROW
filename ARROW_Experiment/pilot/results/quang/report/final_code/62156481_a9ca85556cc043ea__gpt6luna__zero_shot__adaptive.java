package com.palantir.crypto2.keys.serialization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.palantir.crypto2.keys.KeyMaterial;
import java.security.InvalidKeyException;
import javax.crypto.SecretKey;
import org.junit.Test;

public class KeyMaterials_ArrowTest {

    @Test
    public void generateKeyCreatesKeyWithRequestedAlgorithmAndSize() {
        SecretKey key = KeyMaterials.generateKey("AES", 128);

        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
        assertEquals(16, key.getEncoded().length);
    }

    @Test
    public void generateIvCreatesRequestedNumberOfBytes() {
        byte[] iv = KeyMaterials.generateIv(12);

        assertNotNull(iv);
        assertEquals(12, iv.length);
    }

    @Test
    public void generateIvAllowsEmptyIv() {
        assertArrayEquals(new byte[0], KeyMaterials.generateIv(0));
    }

    @Test
    public void generateKeyMaterialCreatesKeyAndIv() {
        KeyMaterial material = KeyMaterials.generateKeyMaterial("AES", 128, 16);

        assertNotNull(material);
        assertEquals("AES", material.getSecretKey().getAlgorithm());
        assertEquals(16, material.getSecretKey().getEncoded().length);
        assertEquals(16, material.getIv().length);
    }

    @Test
    public void fromCreatesMaterialFromEncodedKeyAndIv() {
        byte[] encodedKey = new byte[] {1, 2, 3, 4};
        byte[] iv = new byte[] {5, 6, 7};

        KeyMaterial material = KeyMaterials.from("AES", encodedKey, iv);

        assertEquals("AES", material.getSecretKey().getAlgorithm());
        assertArrayEquals(encodedKey, material.getSecretKey().getEncoded());
        assertArrayEquals(iv, material.getIv());
    }

    @Test
    public void getSafeKeyLengthReturnsRequestedLengthWhenWithinMaximum() throws Exception {
        assertEquals(128, KeyMaterials.getSafeKeyLength("AES", 128));
    }

    @Test
    public void getSafeKeyLengthReturnsSmallerRequestedLength() throws Exception {
        assertEquals(64, KeyMaterials.getSafeKeyLength("AES", 64));
    }

    @Test
    public void getSafeKeyLengthThrowsForUnsupportedAlgorithm() {
        try {
            KeyMaterials.getSafeKeyLength("not-a-real-algorithm", 128);
        } catch (RuntimeException expected) {
            assertTrue(expected.getCause() instanceof java.security.NoSuchAlgorithmException);
            return;
        } catch (InvalidKeyException unexpected) {
            throw new AssertionError(unexpected);
        }
        throw new AssertionError("Expected an exception for an unsupported algorithm");
    }

    @Test(expected = NegativeArraySizeException.class)
    public void generateIvRejectsNegativeSize() {
        KeyMaterials.generateIv(-1);
    }
}
