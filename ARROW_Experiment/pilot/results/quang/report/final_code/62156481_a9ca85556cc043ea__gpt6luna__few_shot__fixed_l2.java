package com.palantir.crypto2.keys.serialization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.palantir.crypto2.keys.KeyMaterial;
import java.security.InvalidKeyException;
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

        assertNotNull(iv);
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
        int maximum = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
        int desired = Math.min(128, maximum);

        assertEquals(desired, KeyMaterials.getSafeKeyLength("AES", desired));
    }

    @Test
    public void rejectsSafeKeyLengthAboveMaximum() throws Exception {
        int maximum = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
        if (maximum == Integer.MAX_VALUE) {
            assertEquals(maximum, KeyMaterials.getSafeKeyLength("AES", maximum));
            return;
        }

        try {
            KeyMaterials.getSafeKeyLength("AES", maximum + 1);
        } catch (InvalidKeyException expected) {
            assertEquals(
                    "Requested key length " + (maximum + 1) + " exceeds JVM allowable key length "
                            + maximum + " for AES",
                    expected.getMessage());
            return;
        }

        assertEquals(maximum, KeyMaterials.getSafeKeyLength("AES", maximum));
    }

    @Test
    public void generatesKeyMaterial() {
        KeyMaterial material = KeyMaterials.generateKeyMaterial("AES", 128, 16);

        assertNotNull(material);
        assertEquals(16, material.getSecretKey().getEncoded().length);
        assertEquals(16, material.getIv().length);
    }
}
