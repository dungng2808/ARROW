package org.stellar.sdk;

import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import org.junit.Test;
import org.stellar.sdk.xdr.DecoratedSignature;
import org.stellar.sdk.xdr.PublicKey;
import org.stellar.sdk.xdr.SignerKey;

import java.security.SignatureException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class KeyPair_ArrowTest {

    @Test
    public void testCanSignReturnsTrueWhenPrivateKeyPresent() {
        KeyPair keyPair = KeyPair.random();
        assertTrue(keyPair.canSign());
    }

    @Test
    public void testCanSignReturnsFalseWhenPrivateKeyNull() {
        EdDSAPublicKey publicKey = new EdDSAPublicKey(
                new net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(
                        new byte[32], EdDSANamedCurveTable.ED_25519_CURVE_SPEC));
        KeyPair keyPair = new KeyPair(publicKey);
        assertFalse(keyPair.canSign());
    }

    @Test
    public void testFromSecretSeedCharArray() {
        char[] seed = "SDJHRQF4G5S7XZP2V6V5N3K5Y5R5T5U5V5W5X5Y5Z5A5B5C5D5E5F5G5H5I5J5K5L5M5N5O5P5Q5R5S5T5U5V5W5X5Y5Z5A5B5C5D5E5F5G5H5I5J5K5L5M5N5O5P5Q5R5S5T5U5V5W5X5Y5Z5".toCharArray();
        KeyPair keyPair = KeyPair.fromSecretSeed(seed);
        assertNotNull(keyPair);
        assertNotNull(keyPair.getSecretSeed());
    }

    @Test
    public void testFromSecretSeedString() {
        String seed = "SDJHRQF4G5S7XZP2V6V5N3K5Y5R5T5U5V5W5X5Y5Z5A5B5C5D5E5F5G5H5I5J5K5L5M5N5O5P5Q5R5S5T5U5V5W5X5Y5Z5A5B5C5D5E5F5G5H5I5J5K5L5M5N5O5P5Q5R5S5T5U5V5W5X5Y5Z5";
        KeyPair keyPair = KeyPair.fromSecretSeed(seed);
        assertNotNull(keyPair);
        assertNotNull(keyPair.getSecretSeed());
    }

    @Test
    public void testFromSecretSeedByteArray() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte) 1);
        KeyPair keyPair = KeyPair.fromSecretSeed(seed);
        assertNotNull(keyPair);
        assertNotNull(keyPair.getSecretSeed());
    }

    @Test
    public void testFromAccountId() {
        String accountId = "GBXGQJWVLWOYHFLVTKWV5FGHA3LNYY2JQKM7OAJAUEQFU6LPCSEFVXON";
        KeyPair keyPair = KeyPair.fromAccountId(accountId);
        assertNotNull(keyPair);
        assertEquals(accountId, keyPair.getAccountId());
    }

    @Test
    public void testFromPublicKey() {
        byte[] publicKey = new byte[32];
        Arrays.fill(publicKey, (byte) 1);
        KeyPair keyPair = KeyPair.fromPublicKey(publicKey);
        assertNotNull(keyPair);
        assertArrayEquals(publicKey, keyPair.getPublicKey());
    }

    @Test
    public void testRandom() {
        KeyPair keyPair = KeyPair.random();
        assertNotNull(keyPair);
        assertNotNull(keyPair.getSecretSeed());
        assertNotNull(keyPair.getAccountId());
    }

    @Test
    public void testGetAccountId() {
        KeyPair keyPair = KeyPair.random();
        String accountId = keyPair.getAccountId();
        assertNotNull(accountId);
        assertTrue(accountId.startsWith("G"));
    }

    @Test
    public void testGetSecretSeed() {
        KeyPair keyPair = KeyPair.random();
        char[] secretSeed = keyPair.getSecretSeed();
        assertNotNull(secretSeed);
        assertTrue(secretSeed.length > 0);
    }

    @Test
    public void testGetPublicKey() {
        KeyPair keyPair = KeyPair.random();
        byte[] publicKey = keyPair.getPublicKey();
        assertNotNull(publicKey);
        assertEquals(32, publicKey.length);
    }

    @Test
    public void testGetXdrPublicKey() {
        KeyPair keyPair = KeyPair.random();
        PublicKey xdrPublicKey = keyPair.getXdrPublicKey();
        assertNotNull(xdrPublicKey);
        assertEquals(org.stellar.sdk.xdr.PublicKeyType.PUBLIC_KEY_TYPE_ED25519, xdrPublicKey.getDiscriminant());
    }

    @Test
    public void testGetXdrSignerKey() {
        KeyPair keyPair = KeyPair.random();
        SignerKey xdrSignerKey = keyPair.getXdrSignerKey();
        assertNotNull(xdrSignerKey);
        assertEquals(org.stellar.sdk.xdr.SignerKeyType.SIGNER_KEY_TYPE_ED25519, xdrSignerKey.getDiscriminant());
    }

    @Test
    public void testFromXdrPublicKey() {
        KeyPair keyPair = KeyPair.random();
        PublicKey xdrPublicKey = keyPair.getXdrPublicKey();
        KeyPair fromXdr = KeyPair.fromXdrPublicKey(xdrPublicKey);
        assertNotNull(fromXdr);
        assertArrayEquals(keyPair.getPublicKey(), fromXdr.getPublicKey());
    }

    @Test
    public void testFromXdrSignerKey() {
        KeyPair keyPair = KeyPair.random();
        SignerKey xdrSignerKey = keyPair.getXdrSignerKey();
        KeyPair fromXdr = KeyPair.fromXdrSignerKey(xdrSignerKey);
        assertNotNull(fromXdr);
        assertArrayEquals(keyPair.getPublicKey(), fromXdr.getPublicKey());
    }

    @Test
    public void testSignAndVerify() {
        KeyPair keyPair = KeyPair.random();
        byte[] data = "Hello, World!".getBytes();
        byte[] signature = keyPair.sign(data);
        assertNotNull(signature);
        assertTrue(keyPair.verify(data, signature));
    }

    @Test(expected = RuntimeException.class)
    public void testSignWithoutPrivateKeyThrowsException() {
        EdDSAPublicKey publicKey = new EdDSAPublicKey(
                new net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(
                        new byte[32], EdDSANamedCurveTable.ED_25519_CURVE_SPEC));
        KeyPair keyPair = new KeyPair(publicKey);
        keyPair.sign("test".getBytes());
    }

    @Test
    public void testSignDecorated() {
        KeyPair keyPair = KeyPair.random();
        byte[] data = "Hello, World!".getBytes();
        DecoratedSignature decoratedSignature = keyPair.signDecorated(data);
        assertNotNull(decoratedSignature);
        assertNotNull(decoratedSignature.getHint());
        assertNotNull(decoratedSignature.getSignature());
    }

    @Test
    public void testVerifyWithInvalidSignatureReturnsFalse() {
        KeyPair keyPair = KeyPair.random();
        byte[] data = "Hello, World!".getBytes();
        byte[] invalidSignature = new byte[64];
        Arrays.fill(invalidSignature, (byte) 1);
        assertFalse(keyPair.verify(data, invalidSignature));
    }

    @Test
    public void testEqualsAndHashCode() {
        KeyPair keyPair1 = KeyPair.random();
        KeyPair keyPair2 = KeyPair.random();
        KeyPair keyPair3 = new KeyPair(keyPair1.mPublicKey, keyPair1.mPrivateKey);

        assertEquals(keyPair1, keyPair3);
        assertEquals(keyPair1.hashCode(), keyPair3.hashCode());

        assertNotEquals(keyPair1, keyPair2);
        assertNotEquals(keyPair1.hashCode(), keyPair2.hashCode());
    }

    @Test
    public void testGetSignatureHint() {
        KeyPair keyPair = KeyPair.random();
        org.stellar.sdk.xdr.SignatureHint signatureHint = keyPair.getSignatureHint();
        assertNotNull(signatureHint);
        assertNotNull(signatureHint.getSignatureHint());
        assertEquals(4, signatureHint.getSignatureHint().length);
    }
}
