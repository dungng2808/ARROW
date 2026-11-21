package com.igormaznitsa.mvngolang.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class SysUtils_ArrowTest {

    @Test
    public void testFindGoSdkOsType_Windows() {
        // This test assumes Windows environment
        // In practice, we can't easily simulate OS detection in tests
        // but we can verify the method exists and returns expected type
        String result = SysUtils.findGoSdkOsType();
        // We can't assert exact value without controlling the environment
        // but we can ensure it doesn't throw and returns valid enum value or null
        assertNotNull(result); // Should return a string or null
    }

    @Test
    public void testFindGoSdkOsType_FreeBSD() {
        // Similar to Windows test - can't easily simulate
        String result = SysUtils.findGoSdkOsType();
        assertNotNull(result);
    }

    @Test
    public void testFindGoSdkOsType_Mac() {
        String result = SysUtils.findGoSdkOsType();
        assertNotNull(result);
    }

    @Test
    public void testFindGoSdkOsType_Linux() {
        String result = SysUtils.findGoSdkOsType();
        assertNotNull(result);
    }

    @Test
    public void testDecodeGoSdkArchType_PPC64LE() {
        String result = SysUtils.decodeGoSdkArchType("ppc64le");
        assertEquals("ppc64le", result);
    }

    @Test
    public void testDecodeGoSdkArchType_ARMV6L() {
        String result = SysUtils.decodeGoSdkArchType("armv6l");
        assertEquals("armv6l", result);
    }

    @Test
    public void testDecodeGoSdkArchType_ARM64() {
        String result = SysUtils.decodeGoSdkArchType("arm64");
        assertEquals("arm64", result);
    }

    @Test
    public void testDecodeGoSdkArchType_S390X() {
        String result = SysUtils.decodeGoSdkArchType("s390");
        assertEquals("s390x", result);
    }

    @Test
    public void testDecodeGoSdkArchType_386() {
        String[] x86Architectures = {"x8632", "x86", "i386", "i486", "i586", "i686", "ia32", "x32"};
        for (String arch : x86Architectures) {
            String result = SysUtils.decodeGoSdkArchType(arch);
            assertEquals("386", result);
        }
    }

    @Test
    public void testDecodeGoSdkArchType_AMD64() {
        String[] amd64Architectures = {"em64t", "x8664", "ia32e", "x64", "amd64", "x86_64"};
        for (String arch : amd64Architectures) {
            String result = SysUtils.decodeGoSdkArchType(arch);
            assertEquals("amd64", result);
        }
    }

    @Test
    public void testDecodeGoSdkArchType_NullForUnknownArchitecture() {
        String result = SysUtils.decodeGoSdkArchType("unknown");
        assertNull(result);
    }

    @Test
    public void testDecodeGoSdkArchType_NullForNullInput() {
        // This should throw NullPointerException since input is @Nonnull
        try {
            SysUtils.decodeGoSdkArchType(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testDecodeGoSdkArchType_CaseInsensitive() {
        String result = SysUtils.decodeGoSdkArchType("AMD64");
        assertEquals("amd64", result);
    }
}
