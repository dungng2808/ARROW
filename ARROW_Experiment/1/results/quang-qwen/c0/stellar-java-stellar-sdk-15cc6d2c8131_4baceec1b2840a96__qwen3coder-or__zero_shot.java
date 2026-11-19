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
        KeyPair keyPair = KeyPair.random();
        char[] seed = keyPair.getSecretSeed();
        KeyPair fromSeed = KeyPair.fromSecretSeed(seed);
        assertNotNull(fromSeed);
    }

    @Test
    public void testFromSecretSeedString() {
        KeyPair keyPair = KeyPair.random();
        String seed = new String(keyPair.getSecretSeed());
        KeyPair fromSeed = KeyPair.fromSecretSeed(seed);
        assertNotNull(fromSeed);
    }

    @Test
    public void testFromSecretSeedByteArray() {
        KeyPair keyPair = KeyPair.random();
        byte[] seed = keyPair.getSecretSeed();
        KeyPair fromSeed = KeyPair.fromSecretSeed(seed);
        assertNotNull(fromSeed);
    }

    @Test
    public void testFromAccountId() {
        KeyPair keyPair = KeyPair.random();
        String accountId = keyPair.getAccountId();
        KeyPair fromAccountId = KeyPair.fromAccountId(accountId);
        assertNotNull(fromAccountId);
    }

    @Test
    public void testFromPublicKey() {
        KeyPair keyPair = KeyPair.random();
        byte[] publicKey = keyPair.getPublicKey();
        KeyPair fromPublicKey = KeyPair.fromPublicKey(publicKey);
        assertNotNull(fromPublicKey);
    }

    @Test
    public void testRandom() {
        KeyPair keyPair = KeyPair.random();
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPublicKey());
        assertNotNull(keyPair.getSecretSeed());
    }

    @Test
    public void testGetAccountId() {
        KeyPair keyPair = KeyPair.random();
        String accountId = keyPair.getAccountId();
        assertNotNull(accountId);
        assertTrue(accountId.length() > 0);
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
    public void testGetSignatureHint() {
        KeyPair keyPair = KeyPair.random();
        org.stellar.sdk.xdr.SignatureHint signatureHint = keyPair.getSignatureHint();
        assertNotNull(signatureHint);
        assertNotNull(signatureHint.getSignatureHint());
        assertEquals(4, signatureHint.getSignatureHint().length);
    }

    @Test
    public void testGetXdrPublicKey() {
        KeyPair keyPair = KeyPair.random();
        PublicKey publicKey = keyPair.getXdrPublicKey();
        assertNotNull(publicKey);
        assertEquals(org.stellar.sdk.xdr.PublicKeyType.PUBLIC_KEY_TYPE_ED25519, publicKey.getDiscriminant());
    }

    @Test
    public void testGetXdrSignerKey() {
        KeyPair keyPair = KeyPair.random();
        SignerKey signerKey = keyPair.getXdrSignerKey();
        assertNotNull(signerKey);
        assertEquals(org.stellar.sdk.xdr.SignerKeyType.SIGNER_KEY_TYPE_ED25519, signerKey.getDiscriminant());
    }

    @Test
    public void testFromXdrPublicKey() {
        KeyPair keyPair = KeyPair.random();
        PublicKey xdrPublicKey = keyPair.getXdrPublicKey();
        KeyPair fromXdr = KeyPair.fromXdrPublicKey(xdrPublicKey);
        assertNotNull(fromXdr);
    }

    @Test
    public void testFromXdrSignerKey() {
        KeyPair keyPair = KeyPair.random();
        SignerKey xdrSignerKey = keyPair.getXdrSignerKey();
        KeyPair fromXdr = KeyPair.fromXdrSignerKey(xdrSignerKey);
        assertNotNull(fromXdr);
    }

    @Test
    public void testSignAndVerify() {
        KeyPair keyPair = KeyPair.random();
        byte[] data = "test data".getBytes();
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
        keyPair.sign("test data".getBytes());
    }

    @Test
    public void testSignDecorated() {
        KeyPair keyPair = KeyPair.random();
        byte[] data = "test data".getBytes();
        DecoratedSignature decoratedSignature = keyPair.signDecorated(data);
        assertNotNull(decoratedSignature);
        assertNotNull(decoratedSignature.getHint());
        assertNotNull(decoratedSignature.getSignature());
    }

    @Test
    public void testSignPayloadDecorated() {
        KeyPair keyPair = KeyPair.random();
        byte[] payload = "test payload".getBytes();
        DecoratedSignature decoratedSignature = keyPair.signPayloadDecorated(payload);
        assertNotNull(decoratedSignature);
        assertNotNull(decoratedSignature.getHint());
        assertNotNull(decoratedSignature.getSignature());
    }

    @Test
    public void testVerifyWithInvalidSignatureReturnsFalse() {
        KeyPair keyPair = KeyPair.random();
        byte[] data = "test data".getBytes();
        byte[] invalidSignature = new byte[64];
        Arrays.fill(invalidSignature, (byte) 1);
        assertFalse(keyPair.verify(data, invalidSignature));
    }

    @Test
    public void testEqualsAndHashCode() {
        KeyPair keyPair1 = KeyPair.random();
        KeyPair keyPair2 = KeyPair.random();
        KeyPair keyPair3 = KeyPair.fromSecretSeed(keyPair1.getSecretSeed());

        assertEquals(keyPair1, keyPair3);
        assertEquals(keyPair1.hashCode(), keyPair3.hashCode());
        assertNotEquals(keyPair1, keyPair2);
        assertNotEquals(keyPair1.hashCode(), keyPair2.hashCode());
    }

    @Test
    public void testEqualsWithNull() {
        KeyPair keyPair = KeyPair.random();
        assertNotEquals(keyPair, null);
    }

    @Test
    public void testEqualsWithDifferentClass() {
        KeyPair keyPair = KeyPair.random();
        assertNotEquals(keyPair, new Object());
    }
}
