package org.stellar.sdk;

import org.junit.Test;
import org.stellar.sdk.xdr.DecoratedSignature;
import org.stellar.sdk.xdr.PublicKey;
import org.stellar.sdk.xdr.SignerKey;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class KeyPair_ArrowTest {

    private byte[] seed() {
        byte[] seed = new byte[32];
        for (int i = 0; i < seed.length; i++) {
            seed[i] = (byte) i;
        }
        return seed;
    }

    @Test
    public void secretSeedCreatesSigningKeyAndRoundTrips() {
        KeyPair keyPair = KeyPair.fromSecretSeed(seed());

        assertTrue(keyPair.canSign());
        assertEquals(keyPair, KeyPair.fromSecretSeed(keyPair.getSecretSeed()));
        assertEquals(keyPair, KeyPair.fromSecretSeed(new String(keyPair.getSecretSeed())));
        assertEquals(keyPair.getAccountId(), KeyPair.fromAccountId(keyPair.getAccountId()).getAccountId());
        assertEquals(32, keyPair.getPublicKey().length);
    }

    @Test
    public void publicKeyCreatesVerificationOnlyKey() {
        KeyPair signingKey = KeyPair.fromSecretSeed(seed());
        KeyPair publicOnly = KeyPair.fromPublicKey(signingKey.getPublicKey());

        assertFalse(publicOnly.canSign());
        assertArrayEquals(signingKey.getPublicKey(), publicOnly.getPublicKey());
        assertEquals(signingKey.getAccountId(), publicOnly.getAccountId());
        assertNotEquals(signingKey, publicOnly);
    }

    @Test
    public void signsAndVerifiesData() {
        KeyPair keyPair = KeyPair.fromSecretSeed(seed());
        byte[] data = new byte[] {1, 2, 3, 4};
        byte[] signature = keyPair.sign(data);

        assertTrue(keyPair.verify(data, signature));
        assertFalse(keyPair.verify(new byte[] {1, 2, 3, 5}, signature));
    }

    @Test
    public void verificationOnlyKeyRejectsInvalidSignature() {
        KeyPair keyPair = KeyPair.fromPublicKey(KeyPair.fromSecretSeed(seed()).getPublicKey());

        assertFalse(keyPair.verify(new byte[] {1}, new byte[] {1}));
    }

    @Test
    public void signingWithoutSecretThrows() {
        KeyPair keyPair = KeyPair.fromPublicKey(KeyPair.fromSecretSeed(seed()).getPublicKey());

        try {
            keyPair.sign(new byte[] {1});
            fail("Expected signing without a secret key to fail");
        } catch (RuntimeException expected) {
            assertEquals("KeyPair does not contain secret key. Use KeyPair.fromSecretSeed method to create a new KeyPair with a secret key.", expected.getMessage());
        }
    }

    @Test
    public void decoratedSignatureContainsSignatureAndHint() {
        KeyPair keyPair = KeyPair.fromSecretSeed(seed());
        DecoratedSignature decorated = keyPair.signDecorated(new byte[] {1, 2, 3});

        assertNotNull(decorated.getSignature());
        assertEquals(64, decorated.getSignature().getSignature().length);
        assertArrayEquals(keyPair.getSignatureHint().getSignatureHint(), decorated.getHint().getSignatureHint());
    }

    @Test
    public void payloadDecoratedSignatureUsesPayloadAndPublicKeyHints() {
        KeyPair keyPair = KeyPair.fromSecretSeed(seed());
        byte[] payload = new byte[] {1, 2, 3, 4, 5};
        DecoratedSignature decorated = keyPair.signPayloadDecorated(payload);
        byte[] expectedHint = keyPair.getSignatureHint().getSignatureHint();
        byte[] payloadTail = new byte[] {2, 3, 4, 5};

        for (int i = 0; i < expectedHint.length; i++) {
            expectedHint[i] ^= payloadTail[i];
        }

        assertArrayEquals(expectedHint, decorated.getHint().getSignatureHint());
        assertEquals(64, decorated.getSignature().getSignature().length);
    }

    @Test
    public void xdrKeysRoundTrip() {
        KeyPair keyPair = KeyPair.fromSecretSeed(seed());
        PublicKey publicKey = keyPair.getXdrPublicKey();
        SignerKey signerKey = keyPair.getXdrSignerKey();

        assertEquals(keyPair, KeyPair.fromXdrPublicKey(publicKey));
        assertArrayEquals(keyPair.getPublicKey(), KeyPair.fromXdrSignerKey(signerKey).getPublicKey());
    }

    @Test
    public void equalityAndHashCodeReflectKeys() {
        KeyPair first = KeyPair.fromSecretSeed(seed());
        KeyPair same = KeyPair.fromSecretSeed(seed());
        byte[] otherSeed = seed();
        otherSeed[0] = 99;
        KeyPair different = KeyPair.fromSecretSeed(otherSeed);

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertFalse(first.equals(different));
        assertFalse(first.equals("not a key pair"));
    }

    @Test
    public void invalidPublicKeyLengthThrowsRuntimeException() {
        try {
            KeyPair.fromPublicKey(new byte[1]);
            fail("Expected invalid public key to fail");
        } catch (RuntimeException expected) {
            assertEquals("Public key is invalid", expected.getMessage());
        }
    }
}
