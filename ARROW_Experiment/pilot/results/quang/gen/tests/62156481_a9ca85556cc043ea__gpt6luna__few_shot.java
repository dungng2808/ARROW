package com.palantir.crypto2.keys.serialization;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.palantir.crypto2.keys.KeyMaterial;
import java.security.InvalidKeyException;
import java.util.Arrays;
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
        assertArrayEquals(encoded, material.getKey().getEncoded());
        assertArrayEquals(iv, material.getIv());
    }

    @Test
    public void returnsRequestedSafeKeyLengthWhenWithinLimit() throws Exception {
        assertEquals(128, KeyMaterials.getSafeKeyLength("AES", 128));
    }

    @Test
    public void capsSafeKeyLengthAtMaximumWhenRequestedLengthIsVeryLarge() throws Exception {
        int maximum = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");

        try {
            KeyMaterials.getSafeKeyLength("AES", Integer.MAX_VALUE);
        } catch (InvalidKeyException expected) {
            assertEquals(
                    "Requested key length " + Integer.MAX_VALUE + " exceeds JVM allowable key length "
                            + maximum + " for AES",
                    expected.getMessage());
        }
    }

    @Test
    public void generatesKeyMaterial() {
        KeyMaterial material = KeyMaterials.generateKeyMaterial("AES", 128, 16);

        assertNotNull(material);
        assertEquals(16, material.getKey().getEncoded().length);
        assertEquals(16, material.getIv().length);
    }
}
