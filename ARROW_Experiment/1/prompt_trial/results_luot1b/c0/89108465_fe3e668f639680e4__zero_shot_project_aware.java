package com.ongres.scram.common.util;

import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class CryptoUtil_ArrowTest {

    @Test
    public void nonceUsesPrintableCharactersAndExcludesComma() {
        String nonce = CryptoUtil.nonce(100, new SecureRandom());
        assertEquals(100, nonce.length());
        for (int i = 0; i < nonce.length(); i++) {
            char c = nonce.charAt(i);
            assertFalse(c < 0x21 || c > 0x7e || c == ',');
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonceRejectsZeroSize() {
        CryptoUtil.nonce(0, new SecureRandom());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonceRejectsNegativeSize() {
        CryptoUtil.nonce(-1, new SecureRandom());
    }

    @Test
    public void defaultNonceHasRequestedLength() {
        assertEquals(8, CryptoUtil.nonce(8).length());
    }

    @Test
    public void hiMatchesPbkdf2WithHmacSha1() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] result = CryptoUtil.hi(factory, 160, "password".toCharArray(),
                "salt".getBytes("UTF-8"), 1);
        assertArrayEquals(new byte[] {
                0x0c, 0x60, (byte) 0xc8, 0x0f, (byte) 0x96, 0x1f, 0x0e, 0x71,
                (byte) 0xf3, (byte) 0xa9, (byte) 0xb5, 0x24, (byte) 0xaf, 0x60,
                0x12, 0x06, 0x2f, (byte) 0xe0, 0x37, (byte) 0xa6
        }, result);
    }

    @Test
    public void hmacMatchesKnownHmacSha1Value() throws Exception {
        SecretKeySpec key = new SecretKeySpec("key".getBytes("UTF-8"), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        byte[] result = CryptoUtil.hmac(key, mac,
                "The quick brown fox jumps over the lazy dog".getBytes("UTF-8"));
        assertArrayEquals(new byte[] {
                (byte) 0xde, 0x7c, (byte) 0x9b, (byte) 0x85, (byte) 0xb8, (byte) 0xb7,
                (byte) 0x8a, (byte) 0xa6, (byte) 0xbc, (byte) 0x8a, 0x7a, 0x36,
                (byte) 0xf7, 0x0a, (byte) 0x90, 0x70, 0x1c, (byte) 0x9d, (byte) 0xb4,
                (byte) 0xd9
        }, result);
    }

    @Test
    public void xorCombinesBytes() {
        assertArrayEquals(new byte[] {0x04, 0x04, 0x04},
                CryptoUtil.xor(new byte[] {0x01, 0x02, 0x03},
                        new byte[] {0x05, 0x06, 0x07}));
    }

    @Test
    public void xorSupportsEmptyArrays() {
        assertArrayEquals(new byte[0], CryptoUtil.xor(new byte[0], new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void xorRejectsDifferentLengths() {
        CryptoUtil.xor(new byte[] {1}, new byte[0]);
    }

    @Test(expected = NullPointerException.class)
    public void xorRejectsNullFirstValue() {
        CryptoUtil.xor(null, new byte[0]);
    }

    @Test(expected = NullPointerException.class)
    public void xorRejectsNullSecondValue() {
        CryptoUtil.xor(new byte[0], null);
    }
}
