package com.palantir.crypto2.keys.serialization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.palantir.crypto2.keys.KeyMaterial;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import org.junit.Test;

public class KeyMaterials_ArrowTest {

    @Test
    public void generatesAesKey() {
        SecretKey key = KeyMaterials.generateKey("AES", 128);

        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
        assertEquals(16, key.getEncoded().length);
    }

    @Test
    public void generatesIvOfRequestedLength() {
        byte[] iv = KeyMaterials.generateIv(12);

        assertEquals(12, iv.length);
    }

    @Test
    public void createsKeyMaterialFromEncodedKeyAndIv() {
        byte[] encoded = new byte[] {1, 2, 3, 4};
        byte[] iv = new byte[] {5, 6, 7};

        KeyMaterial material = KeyMaterials.from("AES", encoded, iv);

        assertNotNull(material);
        assertArrayEquals(encoded, material.getSecretKey().getEncoded());
        assertArrayEquals(iv, material.getIv());
    }

    @Test
    public void returnsRequestedSafeKeyLengthWhenWithinLimit() throws Exception {
        assertEquals(128, KeyMaterials.getSafeKeyLength("AES", 128));
    }

    @Test
    public void returnsMaximumSafeKeyLengthWhenOverrideIsEnabled() throws NoSuchAlgorithmException, InvalidKeyException {
        int maximum = Cipher.getMaxAllowedKeyLength("AES");

        assertEquals(maximum, KeyMaterials.getSafeKeyLength("AES", maximum));
    }

    @Test
    public void generatesKeyMaterial() {
        KeyMaterial material = KeyMaterials.generateKeyMaterial("AES", 128, 16);

        assertNotNull(material);
        assertEquals(16, material.getSecretKey().getEncoded().length);
        assertEquals(16, material.getIv().length);
    }

    @Test
    public void rejectsKeyLengthAboveMaximumWhenOverrideIsNotEnabled() throws Exception {
        int maximum = Cipher.getMaxAllowedKeyLength("AES");

        if (maximum < Integer.MAX_VALUE) {
            try {
                KeyMaterials.getSafeKeyLength("AES", maximum + 1);
            } catch (InvalidKeyException expected) {
                assertNotNull(expected);
                return;
            }
        }

        assertEquals(maximum, KeyMaterials.getSafeKeyLength("AES", maximum));
    }
}
