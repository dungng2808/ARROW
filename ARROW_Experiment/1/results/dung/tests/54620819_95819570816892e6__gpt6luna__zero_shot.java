package com.igormaznitsa.mvngolang.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SysUtils_ArrowTest {

  @Test
  public void decodesPpc64le() {
    assertEquals("ppc64le", SysUtils.decodeGoSdkArchType("PPC64LE"));
  }

  @Test
  public void decodesArmv6l() {
    assertEquals("armv6l", SysUtils.decodeGoSdkArchType("armv6l"));
  }

  @Test
  public void decodesArm64() {
    assertEquals("arm64", SysUtils.decodeGoSdkArchType("aarch64-arm64"));
  }

  @Test
  public void decodesS390AsS390x() {
    assertEquals("s390x", SysUtils.decodeGoSdkArchType("s390"));
  }

  @Test
  public void decodes32BitArchitectures() {
    assertEquals("386", SysUtils.decodeGoSdkArchType("i686"));
  }

  @Test
  public void decodes64BitArchitectures() {
    assertEquals("amd64", SysUtils.decodeGoSdkArchType("x86_64"));
  }

  @Test
  public void returnsNullForUnknownArchitecture() {
    assertNull(SysUtils.decodeGoSdkArchType("mips"));
  }

  @Test
  public void findsOperatingSystemType() {
    String osType = SysUtils.findGoSdkOsType();
    if (osType == null) {
      assertNull(osType);
    } else {
      assertEquals(true, "windows".equals(osType) || "freebsd".equals(osType)
          || "darwin".equals(osType) || "linux".equals(osType));
    }
  }
}
