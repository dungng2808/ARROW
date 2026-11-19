package com.ongres.scram.common.util;

import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class CryptoUtil_ArrowTest {

    @Test
    public void nonceUsesPrintableCharactersAndExcludesComma() {
        String nonce = CryptoUtil.nonce(100, new SecureRandom());
        assertEquals(100, nonce.length());
        for (int i = 0; i < nonce.length(); i++) {
            char c = nonce.charAt(i);
            assertTruePrintableAndNotComma(c);
        }
    }

    private void assertTruePrintableAndNotComma(char c) {
        org.junit.Assert.assertTrue(c >= 0x21 && c <= 0x7e && c != ',');
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
    public void nonceWithDefaultRandomHasRequestedLength() {
        String nonce = CryptoUtil.nonce(12);
        assertEquals(12, nonce.length());
    }

    @Test
    public void hiMatchesPbkdf2HmacSha1() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] result = CryptoUtil.hi(factory, 160, "password".toCharArray(),
                "salt".getBytes("UTF-8"), 1);
        assertArrayEquals(new byte[] {
                (byte) 0x0c, (byte) 0x60, (byte) 0xc8, (byte) 0x0f, (byte) 0x96,
                (byte) 0x1f, (byte) 0x0e, (byte) 0x71, (byte) 0xf3, (byte) 0xa9,
                (byte) 0xb5, (byte) 0x24, (byte) 0xaf, (byte) 0x60, (byte) 0x12,
                (byte) 0x06, (byte) 0x2f, (byte) 0xe0, (byte) 0x37, (byte) 0xa6
        }, result);
    }

    @Test
    public void hmacComputesSha256Digest() throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec("key".getBytes("UTF-8"), "HmacSHA256");
        byte[] result = CryptoUtil.hmac(key, mac, "The quick brown fox jumps over the lazy dog".getBytes("UTF-8"));
        assertArrayEquals(new byte[] {
                (byte) 0xf7, (byte) 0xbc, (byte) 0x83, (byte) 0xf4, (byte) 0x30,
                (byte) 0x53, (byte) 0x84, (byte) 0x24, (byte) 0xb1, (byte) 0x32,
                (byte) 0x98, (byte) 0xe6, (byte) 0xaa, (byte) 0x6f, (byte) 0xb1,
                (byte) 0x43, (byte) 0xef, (byte) 0x4d, (byte) 0x59, (byte) 0xa1,
                (byte) 0x49, (byte) 0x46, (byte) 0x17, (byte) 0x59, (byte) 0x97,
                (byte) 0x47, (byte) 0x9d, (byte) 0xbc, (byte) 0x2d, (byte) 0x1a,
                (byte) 0x3c, (byte) 0xd8
        }, result);
    }

    @Test
    public void xorCombinesBytes() {
        assertArrayEquals(new byte[] { (byte) 0xff, (byte) 0x00, (byte) 0x55 },
                CryptoUtil.xor(new byte[] { (byte) 0xf0, (byte) 0x0f, (byte) 0xaa },
                        new byte[] { (byte) 0x0f, (byte) 0x0f, (byte) 0xff }));
    }

    @Test
    public void xorSupportsEmptyArrays() {
        assertArrayEquals(new byte[0], CryptoUtil.xor(new byte[0], new byte[0]));
    }

    @Test(expected = NullPointerException.class)
    public void xorRejectsNullFirstValue() {
        CryptoUtil.xor(null, new byte[0]);
    }

    @Test(expected = NullPointerException.class)
    public void xorRejectsNullSecondValue() {
        CryptoUtil.xor(new byte[0], null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void xorRejectsDifferentLengths() {
        CryptoUtil.xor(new byte[] { 1 }, new byte[0]);
    }
}
