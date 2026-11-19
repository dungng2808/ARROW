package com.ongres.scram.common.util;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CryptoUtil_ArrowTest {

    @Test
    public void nonceWithSuppliedRandomProducesPrintableCharactersExceptComma() {
        java.security.SecureRandom random = new java.security.SecureRandom() {
            private int next;

            @Override
            public int nextInt(int bound) {
                int[] values = {0, 11, 93};
                return values[next++ % values.length];
            }
        };

        String nonce = CryptoUtil.nonce(3, random);

        assertEquals("!-. ", nonce.substring(0, 3));
    }

    @Test
    public void nonceHasRequestedLengthAndAllowedCharacters() {
        String nonce = CryptoUtil.nonce(100);

        assertEquals(100, nonce.length());
        for (int i = 0; i < nonce.length(); i++) {
            char c = nonce.charAt(i);
            assertTrue(c >= 0x21 && c <= 0x7e);
            assertFalse(c == ',');
        }
    }

    @Test
    public void nonceRejectsNonPositiveSize() {
        try {
            CryptoUtil.nonce(0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("Size must be positive", expected.getMessage());
        }
    }

    @Test
    public void hiComputesPbkdf2HmacSha1() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] result = CryptoUtil.hi(factory, 160, "password".toCharArray(),
                "salt".getBytes(StandardCharsets.UTF_8), 1);

        assertArrayEquals(new byte[] {
                0x0c, 0x60, (byte) 0xc8, 0x0f, (byte) 0x96, 0x1f, 0x0e, 0x71,
                (byte) 0xf3, (byte) 0xa9, (byte) 0xb5, 0x24, (byte) 0xaf, 0x60,
                0x12, 0x06, 0x2f, (byte) 0xe0, 0x37, (byte) 0xa6
        }, result);
    }

    @Test
    public void hmacComputesSha256Mac() throws Exception {
        SecretKeySpec key = new SecretKeySpec("key".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");

        byte[] result = CryptoUtil.hmac(key, mac, "The quick brown fox jumps over the lazy dog"
                .getBytes(StandardCharsets.UTF_8));

        assertArrayEquals(new byte[] {
                (byte) 0xf7, (byte) 0xbc, (byte) 0x83, (byte) 0xf4, 0x30, 0x53, (byte) 0x84, 0x24,
                (byte) 0xb1, (byte) 0x32, (byte) 0x98, (byte) 0xe6, (byte) 0xaa, 0x6f, (byte) 0xb1, 0x43,
                (byte) 0xef, 0x4d, 0x59, (byte) 0xa1, 0x49, 0x46, 0x17, 0x59,
                (byte) 0x97, 0x47, (byte) 0x9d, (byte) 0xbc, 0x2d, 0x1a, 0x3c, (byte) 0xd8
        }, result);
    }

    @Test
    public void xorCombinesBytes() {
        byte[] result = CryptoUtil.xor(new byte[] {0x0f, (byte) 0xaa, 0x00},
                new byte[] {0x33, 0x55, (byte) 0xff});

        assertArrayEquals(new byte[] {0x3c, (byte) 0xff, (byte) 0xff}, result);
    }

    @Test
    public void xorAcceptsEmptyArrays() {
        assertArrayEquals(new byte[0], CryptoUtil.xor(new byte[0], new byte[0]));
    }

    @Test
    public void xorRejectsNullFirstValue() {
        try {
            CryptoUtil.xor(null, new byte[0]);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertEquals("value1", expected.getMessage());
        }
    }

    @Test
    public void xorRejectsNullSecondValue() {
        try {
            CryptoUtil.xor(new byte[0], null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertEquals("value2", expected.getMessage());
        }
    }

    @Test
    public void xorRejectsDifferentLengths() {
        try {
            CryptoUtil.xor(new byte[] {1}, new byte[0]);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("Both values must have the same length", expected.getMessage());
        }
    }
}
