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
        // Given
        EdDSAPublicKey publicKey = new EdDSAPublicKey(
                new net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(
                        new byte[32], EdDSANamedCurveTable.ED_25519_CURVE_SPEC));
        EdDSAPrivateKey privateKey = new EdDSAPrivateKey(
                new net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec(
                        new byte[32], EdDSANamedCurveTable.ED_25519_CURVE_SPEC));

        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        // When
        boolean result = keyPair.canSign();

        // Then
        assertTrue(result);
    }

    @Test
    public void testCanSignReturnsFalseWhenPrivateKeyNull() {
        // Given
        EdDSAPublicKey publicKey = new EdDSAPublicKey(
                new net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(
                        new byte[32], EdDSANamedCurveTable.ED_25519_CURVE_SPEC));

        KeyPair keyPair = new KeyPair(publicKey);

        // When
        boolean result = keyPair.canSign();

        // Then
        assertFalse(result);
    }

    @Test
    public void testFromSecretSeedWithCharArray() {
        // Given
        char[] seed = StrKey.encodeStellarSecretSeed(new byte[32]);

        // When
        KeyPair keyPair = KeyPair.fromSecretSeed(seed);

        // Then
        assertNotNull(keyPair);
        assertNotNull(keyPair.getSecretSeed());
        assertNotNull(keyPair.getAccountId());
    }

    @Test
    public void testFromSecretSeedWithString() {
        // Given
        String seed = StrKey.encodeStellarSecretSeed(new byte[32]);

        // When
        KeyPair keyPair = KeyPair.fromSecretSeed(seed);

        // Then
        assertNotNull(keyPair);
        assertNotNull(keyPair.getSecretSeed());
        assertNotNull(keyPair.getAccountId());
    }

    @Test
    public void testFromSecretSeedWithByteArray() {
        // Given
        byte[] seed = new byte[32];

        // When
        KeyPair keyPair = KeyPair.fromSecretSeed(seed);

        // Then
        assertNotNull(keyPair);
        assertNotNull(keyPair.getSecretSeed());
        assertNotNull(keyPair.getAccountId());
    }

    @Test
    public void testFromAccountId() {
        // Given
        String accountId = "GB6YPGW5JFMQZJYD5B47J73JU53V4KU5J5F5J5F5J5F5J5F5J5F5J5F5";

        // When
        KeyPair keyPair = KeyPair.fromAccountId(accountId);

        // Then
        assertNotNull(keyPair);
        assertEquals(accountId, keyPair.getAccountId());
    }

    @Test
    public void testFromPublicKey() {
        // Given
        byte[] publicKey = new byte[32];

        // When
        KeyPair keyPair = KeyPair.fromPublicKey(publicKey);

        // Then
        assertNotNull(keyPair);
        assertArrayEquals(publicKey, keyPair.getPublicKey());
    }

    @Test
    public void testRandom() {
        // When
        KeyPair keyPair = KeyPair.random();

        // Then
        assertNotNull(keyPair);
        assertNotNull(keyPair.getSecretSeed());
        assertNotNull(keyPair.getAccountId());
        assertTrue(keyPair.canSign());
    }

    @Test
    public void testGetAccountId() {
        // Given
        byte[] publicKey = new byte[32];
        publicKey[0] = 1;
        publicKey[1] = 2;
        publicKey[2] = 3;

        EdDSAPublicKey edDSAPublicKey = new EdDSAPublicKey(
                new net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(
                        publicKey, EdDSANamedCurveTable.ED_25519_CURVE_SPEC));

        KeyPair keyPair = new KeyPair(edDSAPublicKey);

        // When
        String accountId = keyPair.getAccountId();

        // Then
        assertNotNull(accountId);
        assertTrue(accountId.length() > 0);
    }

    @Test
    public void testGetSecretSeed() {
        // Given
        byte[] seed = new byte[32];
        seed[0] = 1;
        seed[1] = 2;
        seed[2] = 3;

        KeyPair keyPair = KeyPair.fromSecretSeed(seed);

        // When
        char[] secretSeed = keyPair.getSecretSeed();

        // Then
        assertNotNull(secretSeed);
        assertTrue(secretSeed.length > 0);
    }

    @Test
    public void testGetPublicKey() {
        // Given
        byte[] publicKey = new byte[32];
        publicKey[0] = 1;
        publicKey[1] = 2;
        publicKey[2] = 3;

        EdDSAPublicKey edDSAPublicKey = new EdDSAPublicKey(
                new net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(
                        publicKey, EdDSANamedCurveTable.ED_25519_CURVE_SPEC));

        KeyPair keyPair = new KeyPair(edDSAPublicKey);

        // When
        byte[] result = keyPair.getPublicKey();

        // Then
        assertArrayEquals(publicKey, result);
    }

    @Test
    public void testGetSignatureHint() {
        // Given
        byte[] publicKey = new byte[32];
        publicKey[0] = 1;
        publicKey[1] = 2;
        publicKey[2] = 3;

        EdDSAPublicKey edDSAPublicKey = new EdDSAPublicKey(
                new net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(
                        publicKey, EdDSANamedCurveTable.ED_25519_CURVE_SPEC));

        KeyPair keyPair = new KeyPair(edDSAPublicKey);

        // When
        org.stellar.sdk.xdr.SignatureHint signatureHint = keyPair.getSignatureHint();

        // Then
        assertNotNull(signatureHint);
        assertNotNull(signatureHint.getSignatureHint());
        assertEquals(4, signatureHint.getSignatureHint().length);
    }

    @Test
    public void testGetXdrPublicKey() {
        // Given
        byte[] publicKey = new byte[32];
        publicKey[0] = 1;
        publicKey[1] = 2;
        publicKey[2] = 3;

        EdDSAPublicKey edDSAPublicKey = new EdDSAPublicKey(
                new net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(
                        publicKey, EdDSANamedCurveTable.ED_25519_CURVE_SPEC));

        KeyPair keyPair = new KeyPair(edDSAPublicKey);

        // When
        PublicKey xdrPublicKey = keyPair.getXdrPublicKey();

        // Then
        assertNotNull(xdrPublicKey);
        assertEquals(org.stellar.sdk.xdr.PublicKeyType.PUBLIC_KEY_TYPE_ED25519,
                xdrPublicKey.getDiscriminant());
    }

    @Test
    public void testGetXdrSignerKey() {
        // Given
        byte[] publicKey = new byte[32];
        publicKey[0] = 1;
        publicKey[1] = 2;
        publicKey[2] = 3;

        EdDSAPublicKey edDSAPublicKey = new EdDSAPublicKey(
                new net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(
                        publicKey, EdDSANamedCurveTable.ED_25519_CURVE_SPEC));

        KeyPair keyPair = new KeyPair(edDSAPublicKey);

        // When
        SignerKey xdrSignerKey = keyPair.getXdrSignerKey();

        // Then
        assertNotNull(xdrSignerKey);
        assertEquals(org.stellar.sdk.xdr.SignerKeyType.SIGNER_KEY_TYPE_ED25519,
                xdrSignerKey.getDiscriminant());
    }

    @Test
    public void testFromXdrPublicKey() {
        // Given
        byte[] publicKey = new byte[32];
        publicKey[0] = 1;
        publicKey[1] = 2;
        publicKey[2] = 3;

        PublicKey xdrPublicKey = new PublicKey();
        xdrPublicKey.setDiscriminant(org.stellar.sdk.xdr.PublicKeyType.PUBLIC_KEY_TYPE_ED25519);
        org.stellar.sdk.xdr.Uint256 uint256 = new org.stellar.sdk.xdr.Uint256();
        uint256.setUint256(publicKey);
        xdrPublicKey.setEd25519(uint256);

        // When
        KeyPair keyPair = KeyPair.fromXdrPublicKey(xdrPublicKey);

        // Then
        assertNotNull(keyPair);
        assertArrayEquals(publicKey, keyPair.getPublicKey());
    }

    @Test
    public void testFromXdrSignerKey() {
        // Given
        byte[] publicKey = new byte[32];
        publicKey[0] = 1;
        publicKey[1] = 2;
        publicKey[2] = 3;

        SignerKey xdrSignerKey = new SignerKey();
        xdrSignerKey.setDiscriminant(org.stellar.sdk.xdr.SignerKeyType.SIGNER_KEY_TYPE_ED25519);
        org.stellar.sdk.xdr.Uint256 uint256 = new org.stellar.sdk.xdr.Uint256();
        uint256.setUint256(publicKey);
        xdrSignerKey.setEd25519(uint256);

        // When
        KeyPair keyPair = KeyPair.fromXdrSignerKey(xdrSignerKey);

        // Then
        assertNotNull(keyPair);
        assertArrayEquals(publicKey, keyPair.getPublicKey());
    }

    @Test
    public void testSignAndVerify() {
        // Given
        byte[] seed = new byte[32];
        seed[0] = 1;
        seed[1] = 2;
        seed[2] = 3;

        KeyPair keyPair = KeyPair.fromSecretSeed(seed);
        byte[] data = "Hello World".getBytes();

        // When
        byte[] signature = keyPair.sign(data);
        boolean verified = keyPair.verify(data, signature);

        // Then
        assertNotNull(signature);
        assertTrue(verified);
    }

    @Test(expected = RuntimeException.class)
    public void testSignThrowsExceptionWhenNoPrivateKey() {
        // Given
        byte[] publicKey = new byte[32];
        publicKey[0] = 1;
        publicKey[1] = 2;
        publicKey[2] = 3;

        EdDSAPublicKey edDSAPublicKey = new EdDSAPublicKey(
                new net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec(
                        publicKey, EdDSANamedCurveTable.ED_25519_CURVE_SPEC));

        KeyPair keyPair = new KeyPair(edDSAPublicKey);
        byte[] data = "Hello World".getBytes();

        // When & Then
        keyPair.sign(data);
    }

    @Test
    public void testSignDecorated() {
        // Given
        byte[] seed = new byte[32];
        seed[0] = 1;
        seed[1] = 2;
        seed[2] = 3;

        KeyPair keyPair = KeyPair.fromSecretSeed(seed);
        byte[] data = "Hello World".getBytes();

        // When
        DecoratedSignature decoratedSignature = keyPair.signDecorated(data);

        // Then
        assertNotNull(decoratedSignature);
        assertNotNull(decoratedSignature.getHint());
        assertNotNull(decoratedSignature.getSignature());
    }

    @Test
    public void testSignPayloadDecorated() {
        // Given
        byte[] seed = new byte[32];
        seed[0] = 1;
        seed[1] = 2;
        seed[2] = 3;

        KeyPair keyPair = KeyPair.fromSecretSeed(seed);
        byte[] payload = "Hello World".getBytes();

        // When
        DecoratedSignature decoratedSignature = keyPair.signPayloadDecorated(payload);

        // Then
        assertNotNull(decoratedSignature);
        assertNotNull(decoratedSignature.getHint());
        assertNotNull(decoratedSignature.getSignature());
    }

    @Test
    public void testVerifyReturnsFalseOnInvalidSignature() {
        // Given
        byte[] seed = new byte[32];
        seed[0] = 1;
        seed[1] = 2;
        seed[2] = 3;

        KeyPair keyPair = KeyPair.fromSecretSeed(seed);
        byte[] data = "Hello World".getBytes();
        byte[] invalidSignature = new byte[64];

        // When
        boolean result = keyPair.verify(data, invalidSignature);

        // Then
        assertFalse(result);
    }

    @Test
    public void testEqualsAndHashCode() {
        // Given
        byte[] seed = new byte[32];
        seed[0] = 1;
        seed[1] = 2;
        seed[2] = 3;

        KeyPair keyPair1 = KeyPair.fromSecretSeed(seed);
        KeyPair keyPair2 = KeyPair.fromSecretSeed(seed);

        // When
        boolean equalsResult = keyPair1.equals(keyPair2);
        int hash1 = keyPair1.hashCode();
        int hash2 = keyPair2.hashCode();

        // Then
        assertTrue(equalsResult);
        assertEquals(hash1, hash2);
    }

    @Test
    public void testEqualsReturnsFalseForDifferentKeys() {
        // Given
        byte[] seed1 = new byte[32];
        seed1[0] = 1;
        byte[] seed2 = new byte[32];
        seed2[0] = 2;

        KeyPair keyPair1 = KeyPair.fromSecretSeed(seed1);
        KeyPair keyPair2 = KeyPair.fromSecretSeed(seed2);

        // When
        boolean result = keyPair1.equals(keyPair2);

        // Then
        assertFalse(result);
    }

    @Test
    public void testEqualsReturnsFalseForNull() {
        // Given
        byte[] seed = new byte[32];
        seed[0] = 1;

        KeyPair keyPair = KeyPair.fromSecretSeed(seed);

        // When
        boolean result = keyPair.equals(null);

        // Then
        assertFalse(result);
    }

    @Test
    public void testEqualsReturnsFalseForDifferentClass() {
        // Given
        byte[] seed = new byte[32];
        seed[0] = 1;

        KeyPair keyPair = KeyPair.fromSecretSeed(seed);

        // When
        boolean result = keyPair.equals("not a keypair");

        // Then
        assertFalse(result);
    }
}
