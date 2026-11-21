package org.stellar.sdk;

import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;
import org.junit.Test;
import org.stellar.sdk.xdr.DecoratedSignature;
import org.stellar.sdk.xdr.PublicKeyType;
import org.stellar.sdk.xdr.SignerKeyType;

import java.util.Arrays;

import static org.junit.Assert.*;

public class KeyPair_ArrowTest {

  private static byte[] seed() {
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
    assertArrayEquals(seed(), KeyPair.fromSecretSeed(keyPair.getSecretSeed()).getSecretSeed() == null
        ? null : seed());
    assertEquals(keyPair, KeyPair.fromSecretSeed(keyPair.getSecretSeed()));
    assertEquals(56, keyPair.getAccountId().length());
  }

  @Test
  public void publicKeyOnlyPairCannotSign() {
    KeyPair original = KeyPair.fromSecretSeed(seed());
    KeyPair publicOnly = KeyPair.fromPublicKey(original.getPublicKey());

    assertFalse(publicOnly.canSign());
    assertArrayEquals(original.getPublicKey(), publicOnly.getPublicKey());
    assertEquals(original.getAccountId(), publicOnly.getAccountId());
  }

  @Test
  public void publicKeyConstructorRejectsNull() {
    try {
      new KeyPair((EdDSAPublicKey) null);
      fail("Expected null public key to be rejected");
    } catch (NullPointerException expected) {
      // Expected.
    }
  }

  @Test
  public void signingAndVerificationWork() {
    KeyPair keyPair = KeyPair.fromSecretSeed(seed());
    byte[] message = new byte[] {1, 2, 3, 4};
    byte[] signature = keyPair.sign(message);

    assertEquals(64, signature.length);
    assertTrue(keyPair.verify(message, signature));
    assertFalse(keyPair.verify(new byte[] {1, 2, 3, 5}, signature));
  }

  @Test
  public void publicOnlyPairCannotSign() {
    KeyPair keyPair = KeyPair.fromPublicKey(KeyPair.fromSecretSeed(seed()).getPublicKey());

    try {
      keyPair.sign(new byte[] {1});
      fail("Expected signing without a secret key to fail");
    } catch (RuntimeException expected) {
      assertEquals("KeyPair does not contain secret key. Use KeyPair.fromSecretSeed method to create a new KeyPair with a secret key.",
          expected.getMessage());
    }
  }

  @Test
  public void xdrKeysAndSignatureHintUsePublicKey() {
    KeyPair keyPair = KeyPair.fromSecretSeed(seed());

    assertEquals(PublicKeyType.PUBLIC_KEY_TYPE_ED25519, keyPair.getXdrPublicKey().getDiscriminant());
    assertEquals(SignerKeyType.SIGNER_KEY_TYPE_ED25519, keyPair.getXdrSignerKey().getDiscriminant());
    assertArrayEquals(keyPair.getPublicKey(), keyPair.getXdrPublicKey().getEd25519().getUint256());
    assertArrayEquals(keyPair.getPublicKey(), keyPair.getXdrSignerKey().getEd25519().getUint256());
    assertEquals(4, keyPair.getSignatureHint().getSignatureHint().length);
  }

  @Test
  public void xdrKeyConversionsPreservePublicKey() {
    KeyPair original = KeyPair.fromSecretSeed(seed());

    assertEquals(original, KeyPair.fromXdrPublicKey(original.getXdrPublicKey()));
    assertEquals(original.getPublicKey(),
        KeyPair.fromXdrSignerKey(original.getXdrSignerKey()).getPublicKey());
  }

  @Test
  public void decoratedSignaturesContainSignatureAndHint() {
    KeyPair keyPair = KeyPair.fromSecretSeed(seed());
    byte[] message = new byte[] {9, 8, 7};
    DecoratedSignature decorated = keyPair.signDecorated(message);

    assertArrayEquals(keyPair.getSignatureHint().getSignatureHint(),
        decorated.getHint().getSignatureHint());
    assertTrue(keyPair.verify(message, decorated.getSignature().getSignature()));
  }

  @Test
  public void payloadDecoratedSignatureXorsPayloadTailWithKeyHint() {
    KeyPair keyPair = KeyPair.fromSecretSeed(seed());
    byte[] payload = new byte[] {1, 2, 3, 4, 5, 6};
    DecoratedSignature decorated = keyPair.signPayloadDecorated(payload);
    byte[] expected = keyPair.getSignatureHint().getSignatureHint();
    byte[] payloadTail = new byte[] {3, 4, 5, 6};

    for (int i = 0; i < expected.length; i++) {
      expected[i] ^= payloadTail[i];
    }

    assertArrayEquals(expected, decorated.getHint().getSignatureHint());
    assertTrue(keyPair.verify(payload, decorated.getSignature().getSignature()));
  }

  @Test
  public void equalityAndHashCodeReflectKeys() {
    KeyPair first = KeyPair.fromSecretSeed(seed());
    KeyPair same = KeyPair.fromSecretSeed(seed());
    KeyPair different = KeyPair.fromSecretSeed(new byte[32]);

    assertEquals(first, same);
    assertEquals(first.hashCode(), same.hashCode());
    assertNotEquals(first, different);
    assertNotEquals(first, null);
    assertNotEquals(first, "not a key pair");
  }

  @Test
  public void invalidPublicKeyLengthIsRejected() {
    try {
      KeyPair.fromPublicKey(new byte[31]);
      fail("Expected invalid public key to be rejected");
    } catch (RuntimeException expected) {
      assertEquals("Public key is invalid", expected.getMessage());
    }
  }
}
