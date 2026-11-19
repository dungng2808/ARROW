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
    public void testCanSignReturnsFalseWhenPrivateKeyAbsent() {
        EdDSAPublicKey publicKey = new EdDSAPublicKey(new byte[32]);
        KeyPair keyPair = new KeyPair(publicKey);
        assertFalse(keyPair.canSign());
    }

    @Test
    public void testGetAccountId() {
        KeyPair keyPair = KeyPair.random();
        String accountId = keyPair.getAccountId();
        assertNotNull(accountId);
        assertTrue(accountId.length() > 0);
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
        byte[] data = "test data".getBytes();
        byte[] signature = keyPair.sign(data);
        assertNotNull(signature);
        assertTrue(keyPair.verify(data, signature));
    }

    @Test(expected = RuntimeException.class)
    public void testSignThrowsExceptionWhenNoPrivateKey() {
        EdDSAPublicKey publicKey = new EdDSAPublicKey(new byte[32]);
        KeyPair keyPair = new KeyPair(publicKey);
        keyPair.sign("test data".getBytes());
    }

    @Test
    public void testVerifyReturnsFalseOnInvalidSignature() {
        KeyPair keyPair = KeyPair.random();
        byte[] data = "test data".getBytes();
        byte[] invalidSignature = new byte[64];
        Arrays.fill(invalidSignature, (byte) 1);
        assertFalse(keyPair.verify(data, invalidSignature));
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
    public void testGetSignatureHint() {
        KeyPair keyPair = KeyPair.random();
        org.stellar.sdk.xdr.SignatureHint signatureHint = keyPair.getSignatureHint();
        assertNotNull(signatureHint);
        assertNotNull(signatureHint.getSignatureHint());
        assertEquals(4, signatureHint.getSignatureHint().length);
    }

    @Test
    public void testFromSecretSeedCharArray() {
        KeyPair keyPair = KeyPair.random();
        char[] seed = keyPair.getSecretSeed();
        KeyPair fromSeed = KeyPair.fromSecretSeed(seed);
        assertNotNull(fromSeed);
        assertArrayEquals(keyPair.getPublicKey(), fromSeed.getPublicKey());
    }

    @Test
    public void testFromSecretSeedString() {
        KeyPair keyPair = KeyPair.random();
        char[] seedChars = keyPair.getSecretSeed();
        String seed = new String(seedChars);
        KeyPair fromSeed = KeyPair.fromSecretSeed(seed);
        assertNotNull(fromSeed);
        assertArrayEquals(keyPair.getPublicKey(), fromSeed.getPublicKey());
    }

    @Test
    public void testFromSecretSeedByteArray() {
        KeyPair keyPair = KeyPair.random();
        char[] seedChars = keyPair.getSecretSeed();
        byte[] seedBytes = StrKey.decodeStellarSecretSeed(seedChars);
        KeyPair fromSeed = KeyPair.fromSecretSeed(seedBytes);
        assertNotNull(fromSeed);
        assertArrayEquals(keyPair.getPublicKey(), fromSeed.getPublicKey());
    }

    @Test
    public void testFromAccountId() {
        KeyPair keyPair = KeyPair.random();
        String accountId = keyPair.getAccountId();
        KeyPair fromAccountId = KeyPair.fromAccountId(accountId);
        assertNotNull(fromAccountId);
        assertArrayEquals(keyPair.getPublicKey(), fromAccountId.getPublicKey());
    }

    @Test
    public void testFromPublicKey() {
        KeyPair keyPair = KeyPair.random();
        byte[] publicKey = keyPair.getPublicKey();
        KeyPair fromPublicKey = KeyPair.fromPublicKey(publicKey);
        assertNotNull(fromPublicKey);
        assertArrayEquals(keyPair.getPublicKey(), fromPublicKey.getPublicKey());
    }

    @Test
    public void testRandom() {
        KeyPair keyPair = KeyPair.random();
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPublicKey());
        assertNotNull(keyPair.getSecretSeed());
        assertTrue(keyPair.canSign());
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
