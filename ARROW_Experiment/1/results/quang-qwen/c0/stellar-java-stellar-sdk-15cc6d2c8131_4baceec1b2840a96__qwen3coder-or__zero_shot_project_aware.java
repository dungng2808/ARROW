package org.stellar.sdk;

import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import org.junit.Test;
import org.stellar.sdk.xdr.DecoratedSignature;
import org.stellar.sdk.xdr.PublicKey;
import org.stellar.sdk.xdr.SignatureHint;
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
        EdDSAPublicKey publicKey = KeyPair.random().mPublicKey;
        KeyPair keyPair = new KeyPair(publicKey);
        assertFalse(keyPair.canSign());
    }

    @Test
    public void testFromSecretSeedCharArray() {
        char[] seed = "SDJHRQF6YK332DZ7D5V353456789012345678901234567890123".toCharArray();
        KeyPair keyPair = KeyPair.fromSecretSeed(seed);
        assertNotNull(keyPair);
        assertNotNull(keyPair.mPrivateKey);
        assertNotNull(keyPair.mPublicKey);
    }

    @Test
    public void testFromSecretSeedString() {
        String seed = "SDJHRQF6YK332DZ7D5V353456789012345678901234567890123";
        KeyPair keyPair = KeyPair.fromSecretSeed(seed);
        assertNotNull(keyPair);
        assertNotNull(keyPair.mPrivateKey);
        assertNotNull(keyPair.mPublicKey);
    }

    @Test
    public void testFromSecretSeedByteArray() {
        byte[] seed = new byte[32];
        Arrays.fill(seed, (byte) 1);
        KeyPair keyPair = KeyPair.fromSecretSeed(seed);
        assertNotNull(keyPair);
        assertNotNull(keyPair.mPrivateKey);
        assertNotNull(keyPair.mPublicKey);
    }

    @Test
    public void testFromAccountId() {
        String accountId = "GCEZWKCA5VLDNRLN3RPRJMRZOX3Z6G5CHCGSNFHEYVXM3XOJMVPKX2X2";
        KeyPair keyPair = KeyPair.fromAccountId(accountId);
        assertNotNull(keyPair);
        assertNotNull(keyPair.mPublicKey);
        assertNull(keyPair.mPrivateKey);
    }

    @Test
    public void testFromPublicKey() {
        byte[] publicKey = new byte[32];
        Arrays.fill(publicKey, (byte) 1);
        KeyPair keyPair = KeyPair.fromPublicKey(publicKey);
        assertNotNull(keyPair);
        assertNotNull(keyPair.mPublicKey);
        assertNull(keyPair.mPrivateKey);
    }

    @Test
    public void testRandom() {
        KeyPair keyPair = KeyPair.random();
        assertNotNull(keyPair);
        assertNotNull(keyPair.mPrivateKey);
        assertNotNull(keyPair.mPublicKey);
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
    public void testGetSignatureHint() {
        KeyPair keyPair = KeyPair.random();
        SignatureHint signatureHint = keyPair.getSignatureHint();
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
        assertNotNull(fromXdr.mPublicKey);
        assertNull(fromXdr.mPrivateKey);
    }

    @Test
    public void testFromXdrSignerKey() {
        KeyPair keyPair = KeyPair.random();
        SignerKey xdrSignerKey = keyPair.getXdrSignerKey();
        KeyPair fromXdr = KeyPair.fromXdrSignerKey(xdrSignerKey);
        assertNotNull(fromXdr);
        assertNotNull(fromXdr.mPublicKey);
        assertNull(fromXdr.mPrivateKey);
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
        EdDSAPublicKey publicKey = KeyPair.random().mPublicKey;
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
    public void testSignPayloadDecorated() {
        KeyPair keyPair = KeyPair.random();
        byte[] payload = "Hello, World!".getBytes();
        DecoratedSignature decoratedSignature = keyPair.signPayloadDecorated(payload);
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
