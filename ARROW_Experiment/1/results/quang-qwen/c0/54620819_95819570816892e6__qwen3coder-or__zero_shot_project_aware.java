package com.igormaznitsa.mvngolang.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.lang3.SystemUtils;

public class SysUtils_ArrowTest {

    @Test
    public void testFindGoSdkOsType_Windows() {
        // Mock Windows environment
        boolean originalIsWindows = SystemUtils.IS_OS_WINDOWS;
        SystemUtils.IS_OS_WINDOWS = true;
        try {
            assertEquals("windows", SysUtils.findGoSdkOsType());
        } finally {
            SystemUtils.IS_OS_WINDOWS = originalIsWindows;
        }
    }

    @Test
    public void testFindGoSdkOsType_FreeBSD() {
        // Mock FreeBSD environment
        boolean originalIsFreeBsd = SystemUtils.IS_OS_FREE_BSD;
        SystemUtils.IS_OS_FREE_BSD = true;
        try {
            assertEquals("freebsd", SysUtils.findGoSdkOsType());
        } finally {
            SystemUtils.IS_OS_FREE_BSD = originalIsFreeBsd;
        }
    }

    @Test
    public void testFindGoSdkOsType_Mac() {
        // Mock Mac environment
        boolean originalIsMac = SystemUtils.IS_OS_MAC;
        boolean originalIsMacOsx = SystemUtils.IS_OS_MAC_OSX;
        SystemUtils.IS_OS_MAC = true;
        SystemUtils.IS_OS_MAC_OSX = true;
        try {
            assertEquals("darwin", SysUtils.findGoSdkOsType());
        } finally {
            SystemUtils.IS_OS_MAC = originalIsMac;
            SystemUtils.IS_OS_MAC_OSX = originalIsMacOsx;
        }
    }

    @Test
    public void testFindGoSdkOsType_Linux() {
        // Mock Linux environment
        boolean originalIsLinux = SystemUtils.IS_OS_LINUX;
        SystemUtils.IS_OS_LINUX = true;
        try {
            assertEquals("linux", SysUtils.findGoSdkOsType());
        } finally {
            SystemUtils.IS_OS_LINUX = originalIsLinux;
        }
    }

    @Test
    public void testFindGoSdkOsType_Unsupported() {
        // Mock unsupported OS
        boolean originalIsWindows = SystemUtils.IS_OS_WINDOWS;
        boolean originalIsFreeBsd = SystemUtils.IS_OS_FREE_BSD;
        boolean originalIsMac = SystemUtils.IS_OS_MAC;
        boolean originalIsMacOsx = SystemUtils.IS_OS_MAC_OSX;
        boolean originalIsLinux = SystemUtils.IS_OS_LINUX;
        
        SystemUtils.IS_OS_WINDOWS = false;
        SystemUtils.IS_OS_FREE_BSD = false;
        SystemUtils.IS_OS_MAC = false;
        SystemUtils.IS_OS_MAC_OSX = false;
        SystemUtils.IS_OS_LINUX = false;
        
        try {
            assertNull(SysUtils.findGoSdkOsType());
        } finally {
            SystemUtils.IS_OS_WINDOWS = originalIsWindows;
            SystemUtils.IS_OS_FREE_BSD = originalIsFreeBsd;
            SystemUtils.IS_OS_MAC = originalIsMac;
            SystemUtils.IS_OS_MAC_OSX = originalIsMacOsx;
            SystemUtils.IS_OS_LINUX = originalIsLinux;
        }
    }

    @Test
    public void testDecodeGoSdkArchType_ppc64le() {
        assertEquals("ppc64le", SysUtils.decodeGoSdkArchType("ppc64le"));
        assertEquals("ppc64le", SysUtils.decodeGoSdkArchType("PPC64LE"));
        assertEquals("ppc64le", SysUtils.decodeGoSdkArchType("ppc64le-test"));
    }

    @Test
    public void testDecodeGoSdkArchType_armv6l() {
        assertEquals("armv6l", SysUtils.decodeGoSdkArchType("armv6l"));
        assertEquals("armv6l", SysUtils.decodeGoSdkArchType("ARMV6L"));
        assertEquals("armv6l", SysUtils.decodeGoSdkArchType("armv6l-test"));
    }

    @Test
    public void testDecodeGoSdkArchType_arm64() {
        assertEquals("arm64", SysUtils.decodeGoSdkArchType("arm64"));
        assertEquals("arm64", SysUtils.decodeGoSdkArchType("ARM64"));
        assertEquals("arm64", SysUtils.decodeGoSdkArchType("aarch64"));
    }

    @Test
    public void testDecodeGoSdkArchType_s390x() {
        assertEquals("s390x", SysUtils.decodeGoSdkArchType("s390"));
        assertEquals("s390x", SysUtils.decodeGoSdkArchType("S390"));
        assertEquals("s390x", SysUtils.decodeGoSdkArchType("s390x"));
    }

    @Test
    public void testDecodeGoSdkArchType_386() {
        assertEquals("386", SysUtils.decodeGoSdkArchType("x8632"));
        assertEquals("386", SysUtils.decodeGoSdkArchType("x86"));
        assertEquals("386", SysUtils.decodeGoSdkArchType("i386"));
        assertEquals("386", SysUtils.decodeGoSdkArchType("i486"));
        assertEquals("386", SysUtils.decodeGoSdkArchType("i586"));
        assertEquals("386", SysUtils.decodeGoSdkArchType("i686"));
        assertEquals("386", SysUtils.decodeGoSdkArchType("ia32"));
        assertEquals("386", SysUtils.decodeGoSdkArchType("x32"));
        assertEquals("386", SysUtils.decodeGoSdkArchType("X8632"));
        assertEquals("386", SysUtils.decodeGoSdkArchType("X86"));
    }

    @Test
    public void testDecodeGoSdkArchType_amd64() {
        assertEquals("amd64", SysUtils.decodeGoSdkArchType("em64t"));
        assertEquals("amd64", SysUtils.decodeGoSdkArchType("x8664"));
        assertEquals("amd64", SysUtils.decodeGoSdkArchType("ia32e"));
        assertEquals("amd64", SysUtils.decodeGoSdkArchType("x64"));
        assertEquals("amd64", SysUtils.decodeGoSdkArchType("amd64"));
        assertEquals("amd64", SysUtils.decodeGoSdkArchType("x86_64"));
        assertEquals("amd64", SysUtils.decodeGoSdkArchType("EM64T"));
        assertEquals("amd64", SysUtils.decodeGoSdkArchType("X8664"));
    }

    @Test
    public void testDecodeGoSdkArchType_Null() {
        assertNull(SysUtils.decodeGoSdkArchType("unknown"));
        assertNull(SysUtils.decodeGoSdkArchType(""));
        assertNull(SysUtils.decodeGoSdkArchType(null));
    }
}
