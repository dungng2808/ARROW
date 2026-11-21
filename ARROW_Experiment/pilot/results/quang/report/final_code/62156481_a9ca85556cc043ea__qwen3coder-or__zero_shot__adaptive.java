package com.palantir.crypto2.keys.serialization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.palantir.crypto2.keys.KeyMaterial;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import org.junit.Test;

public class KeyMaterials_ArrowTest {

    @Test
    public void testGenerateKey() throws Exception {
        SecretKey key = KeyMaterials.generateKey("AES", 128);
        assertNotNull(key);
        assertThat(key.getAlgorithm(), is("AES"));
        assertThat(key.getEncoded().length, is(16));
    }

    @Test
    public void testGenerateIv() {
        byte[] iv = KeyMaterials.generateIv(16);
        assertNotNull(iv);
        assertThat(iv.length, is(16));
    }

    @Test
    public void testGenerateKeyMaterial() throws Exception {
        KeyMaterial keyMaterial = KeyMaterials.generateKeyMaterial("AES", 128, 16);
        assertNotNull(keyMaterial);
        assertNotNull(keyMaterial.getSecretKey());
        assertNotNull(keyMaterial.getIv());
        assertThat(keyMaterial.getIv().length, is(16));
    }

    @Test
    public void testFrom() {
        byte[] keyBytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        byte[] iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        KeyMaterial keyMaterial = KeyMaterials.from("AES", keyBytes, iv);
        assertNotNull(keyMaterial);
        assertNotNull(keyMaterial.getSecretKey());
        assertArrayEquals(iv, keyMaterial.getIv());
    }

    @Test
    public void testWrapAndUnwrap() throws Exception {
        // Generate a key pair for testing
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // Generate a key material
        KeyMaterial originalKeyMaterial = KeyMaterials.generateKeyMaterial("AES", 128, 16);

        // Wrap the key material
        byte[] wrapped = KeyMaterials.wrap(originalKeyMaterial, publicKey);
        assertNotNull(wrapped);

        // Unwrap the key material
        KeyMaterial unwrappedKeyMaterial = KeyMaterials.unwrap(wrapped, privateKey);
        assertNotNull(unwrappedKeyMaterial);
        assertArrayEquals(originalKeyMaterial.getIv(), unwrappedKeyMaterial.getIv());
    }

    @Test
    public void testSymmetricWrapAndUnwrap() throws Exception {
        // Generate a secret key for testing
        SecretKey secretKey = KeyMaterials.generateKey("AES", 128);

        // Generate a key material
        KeyMaterial originalKeyMaterial = KeyMaterials.generateKeyMaterial("AES", 128, 16);

        // Wrap the key material
        byte[] wrapped = KeyMaterials.symmetricWrap(originalKeyMaterial, secretKey);
        assertNotNull(wrapped);

        // Unwrap the key material
        KeyMaterial unwrappedKeyMaterial = KeyMaterials.symmetricUnwrap(wrapped, secretKey);
        assertNotNull(unwrappedKeyMaterial);
        assertArrayEquals(originalKeyMaterial.getIv(), unwrappedKeyMaterial.getIv());
    }

    @Test
    public void testVersion() throws Exception {
        KeyMaterial keyMaterial = KeyMaterials.generateKeyMaterial("AES", 128, 16);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        byte[] wrapped = KeyMaterials.wrap(keyMaterial, publicKey);
        
        // Access version through reflection since it's private
        java.lang.reflect.Method versionMethod = KeyMaterials.class.getDeclaredMethod("version", byte[].class);
        versionMethod.setAccessible(true);
        int version = (int) versionMethod.invoke(null, wrapped);
        assertThat(version, is(2)); // KeySerializerV2
    }

    @Test
    public void testGetSafeKeyLength_NormalCase() throws Exception {
        int safeLength = KeyMaterials.getSafeKeyLength("AES", 128);
        assertTrue(safeLength >= 0);
    }

    @Test(expected = RuntimeException.class)
    public void testGetSafeKeyLength_InvalidKeyLength() throws Exception {
        // This should throw an exception because we're requesting a very large key size
        // that likely exceeds JVM limits
        KeyMaterials.getSafeKeyLength("AES", 1000000);
    }
}
