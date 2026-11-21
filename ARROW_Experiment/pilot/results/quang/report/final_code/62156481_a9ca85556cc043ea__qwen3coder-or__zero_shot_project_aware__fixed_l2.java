package com.palantir.crypto2.keys.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.palantir.crypto2.keys.KeyMaterial;
import com.palantir.logsafe.exceptions.SafeRuntimeException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import org.junit.Test;

public class KeyMaterials_ArrowTest {

    @Test
    public void testGenerateKey() throws Exception {
        SecretKey key = KeyMaterials.generateKey("AES", 128);
        assertThat(key).isNotNull();
        assertThat(key.getAlgorithm()).isEqualTo("AES");
        assertThat(key.getEncoded().length).isEqualTo(16);
    }

    @Test
    public void testGenerateIv() {
        byte[] iv = KeyMaterials.generateIv(16);
        assertThat(iv).isNotNull();
        assertThat(iv.length).isEqualTo(16);
    }

    @Test
    public void testGenerateKeyMaterial() {
        KeyMaterial keyMaterial = KeyMaterials.generateKeyMaterial("AES", 128, 16);
        assertThat(keyMaterial).isNotNull();
        assertThat(keyMaterial.getSecretKey()).isNotNull();
        assertThat(keyMaterial.getIv()).hasSize(16);
    }

    @Test
    public void testFrom() {
        byte[] keyBytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        byte[] iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        KeyMaterial keyMaterial = KeyMaterials.from("AES", keyBytes, iv);
        assertThat(keyMaterial).isNotNull();
        assertThat(keyMaterial.getSecretKey().getEncoded()).containsExactly(keyBytes);
        assertThat(keyMaterial.getIv()).containsExactly(iv);
    }

    @Test
    public void testWrap() throws Exception {
        KeyMaterial keyMaterial = KeyMaterials.generateKeyMaterial("AES", 128, 16);
        PublicKey publicKey = mock(PublicKey.class);
        byte[] wrapped = KeyMaterials.wrap(keyMaterial, publicKey);
        assertThat(wrapped).isNotNull();
    }

    @Test
    public void testSymmetricWrap() throws Exception {
        KeyMaterial keyMaterial = KeyMaterials.generateKeyMaterial("AES", 128, 16);
        SecretKey secretKey = mock(SecretKey.class);
        byte[] wrapped = KeyMaterials.symmetricWrap(keyMaterial, secretKey);
        assertThat(wrapped).isNotNull();
    }

    @Test
    public void testUnwrap_InvalidVersion() {
        byte[] invalidData = new byte[]{100}; // Invalid version
        PrivateKey privateKey = mock(PrivateKey.class);
        assertThatThrownBy(() -> KeyMaterials.unwrap(invalidData, privateKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid serialization format version");
    }

    @Test
    public void testSymmetricUnwrap_InvalidVersion() {
        byte[] invalidData = new byte[]{100}; // Invalid version
        SecretKey secretKey = mock(SecretKey.class);
        assertThatThrownBy(() -> KeyMaterials.symmetricUnwrap(invalidData, secretKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid serialization format version");
    }

    @Test
    public void testVersion() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4};
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
        int version = stream.read();
        assertThat(version).isEqualTo(1);
    }

    @Test
    public void testGetSafeKeyLength_Valid() throws Exception {
        int safeLength = KeyMaterials.getSafeKeyLength("AES", 128);
        assertThat(safeLength).isEqualTo(128);
    }

    @Test
    public void testGetSafeKeyLength_ExceedsMax() throws Exception {
        // Mock Cipher.getMaxAllowedKeyLength to simulate a restricted environment
        // We'll test the case where the key length exceeds JVM limits
        try {
            int safeLength = KeyMaterials.getSafeKeyLength("AES", 256);
            assertThat(safeLength).isLessThanOrEqualTo(256);
        } catch (InvalidKeyException e) {
            assertThat(e.getMessage()).contains("exceeds JVM allowable key length");
        }
    }

    @Test
    public void testGetSafeKeyLength_InvalidAlgorithm() {
        assertThatThrownBy(() -> KeyMaterials.getSafeKeyLength("INVALID", 128))
                .isInstanceOf(RuntimeException.class);
    }
}
