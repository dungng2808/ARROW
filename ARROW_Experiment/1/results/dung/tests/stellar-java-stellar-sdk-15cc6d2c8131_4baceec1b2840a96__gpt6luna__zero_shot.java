package org.stellar.sdk;

import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import org.junit.Test;
import org.stellar.sdk.xdr.PublicKey;
import org.stellar.sdk.xdr.SignerKey;

import java.util.Arrays;

import static org.junit.Assert.*;

public class KeyPair_ArrowTest {
  private byte[] seed() {
    return new byte[32];
  }

  private KeyPair seededPair() {
    return KeyPair.fromSecretSeed(seed());
  }

  @Test
  public void secretSeedCreatesSigningPair() {
    KeyPair pair = seededPair();
    assertTrue(pair.canSign());
    assertEquals(32, pair.getPublicKey().length);
    assertEquals(pair, KeyPair.fromSecretSeed(seed()));
  }

  @Test
  public void publicKeyCreatesVerificationOnlyPair() {
    KeyPair signingPair = seededPair();
    KeyPair publicOnly = KeyPair.fromPublicKey(signingPair.getPublicKey());
    assertFalse(publicOnly.canSign());
    assertArrayEquals(signingPair.getPublicKey(), publicOnly.getPublicKey());
    assertEquals(signingPair.getAccountId(), publicOnly.getAccountId());
  }

  @Test
  public void accountIdRoundTrips() {
    KeyPair pair = seededPair();
    assertEquals(pair, KeyPair.fromAccountId(pair.getAccountId()));
  }

  @Test
  public void secretSeedStringRoundTrips() {
    KeyPair pair = seededPair();
    assertEquals(pair, KeyPair.fromSecretSeed(new String(pair.getSecretSeed())));
  }

  @Test
  public void xdrPublicKeyRoundTrips() {
    KeyPair pair = seededPair();
    PublicKey xdr = pair.getXdrPublicKey();
    assertEquals(pair, KeyPair.fromXdrPublicKey(xdr));
    assertArrayEquals(pair.getPublicKey(), xdr.getEd25519().getUint256());
  }

  @Test
  public void xdrSignerKeyRoundTrips() {
    KeyPair pair = seededPair();
    SignerKey xdr = pair.getXdrSignerKey();
    assertEquals(pair, KeyPair.fromXdrSignerKey(xdr));
    assertArrayEquals(pair.getPublicKey(), xdr.getEd25519().getUint256());
  }

  @Test
  public void signsAndVerifiesData() {
    KeyPair pair = seededPair();
    byte[] data = new byte[] {1, 2, 3};
    byte[] signature = pair.sign(data);
    assertTrue(pair.verify(data, signature));
    assertFalse(pair.verify(new byte[] {1, 2, 4}, signature));
  }

  @Test
  public void publicOnlyPairVerifiesSignature() {
    KeyPair pair = seededPair();
    byte[] data = new byte[] {4, 5, 6};
    byte[] signature = pair.sign(data);
    assertTrue(KeyPair.fromPublicKey(pair.getPublicKey()).verify(data, signature));
  }

  @Test
  public void invalidSignatureReturnsFalse() {
    KeyPair pair = seededPair();
    assertFalse(pair.verify(new byte[] {1}, new byte[] {1}));
  }

  @Test
  public void decoratedSignatureContainsSignatureAndHint() {
    KeyPair pair = seededPair();
    byte[] data = new byte[] {7, 8};
    org.stellar.sdk.xdr.DecoratedSignature decorated = pair.signDecorated(data);
    assertTrue(pair.verify(data, decorated.getSignature().getSignature()));
    assertArrayEquals(pair.getSignatureHint().getSignatureHint(),
        decorated.getHint().getSignatureHint());
  }

  @Test
  public void payloadDecoratedSignatureUsesPayloadHintXorPublicHint() {
    KeyPair pair = seededPair();
    byte[] payload = new byte[] {1, 2, 3, 4, 5};
    org.stellar.sdk.xdr.DecoratedSignature decorated = pair.signPayloadDecorated(payload);
    byte[] expected = pair.getSignatureHint().getSignatureHint().clone();
    for (int i = 0; i < 4; i++) {
      expected[i] ^= payload[i + 1];
    }
    assertArrayEquals(expected, decorated.getHint().getSignatureHint());
    assertTrue(pair.verify(payload, decorated.getSignature().getSignature()));
  }

  @Test
  public void payloadShorterThanHintIsZeroPaddedBeforeXor() {
    KeyPair pair = seededPair();
    byte[] payload = new byte[] {9, 10};
    org.stellar.sdk.xdr.DecoratedSignature decorated = pair.signPayloadDecorated(payload);
    byte[] expected = pair.getSignatureHint().getSignatureHint().clone();
    expected[0] ^= 9;
    expected[1] ^= 10;
    assertArrayEquals(expected, decorated.getHint().getSignatureHint());
  }

  @Test
  public void equalPairsHaveEqualHashCodes() {
    KeyPair first = seededPair();
    KeyPair second = KeyPair.fromSecretSeed(seed());
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
    assertFalse(first.equals(null));
    assertFalse(first.equals("not a key pair"));
  }

  @Test
  public void publicOnlyPairsCompareEqual() {
    KeyPair pair = seededPair();
    KeyPair first = KeyPair.fromPublicKey(pair.getPublicKey());
    KeyPair second = KeyPair.fromPublicKey(pair.getPublicKey());
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test(expected = RuntimeException.class)
  public void publicOnlyPairCannotSign() {
    KeyPair.fromPublicKey(new byte[32]).sign(new byte[0]);
  }

  @Test(expected = RuntimeException.class)
  public void invalidPublicKeyLengthIsRejected() {
    KeyPair.fromPublicKey(new byte[31]);
  }

  @Test(expected = NullPointerException.class)
  public void nullPublicKeyIsRejected() {
    new KeyPair((EdDSAPublicKey) null);
  }

  @Test
  public void explicitPublicAndPrivateConstructorCanSign() {
    byte[] seed = seed();
    EdDSAPrivateKeySpec privateSpec =
        new EdDSAPrivateKeySpec(seed, EdDSANamedCurveTable.ED_25519_CURVE_SPEC);
    EdDSAPublicKeySpec publicSpec =
        new EdDSAPublicKeySpec(privateSpec.getA().toByteArray(),
            EdDSANamedCurveTable.ED_25519_CURVE_SPEC);
    KeyPair pair = new KeyPair(new EdDSAPublicKey(publicSpec),
        new EdDSAPrivateKey(privateSpec));
    assertTrue(pair.canSign());
    assertTrue(pair.verify(new byte[] {3}, pair.sign(new byte[] {3})));
  }
}
