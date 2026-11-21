package com.igormaznitsa.mvngolang.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SysUtils_ArrowTest {

  @Test
  public void decodesPpc64leArchitecture() {
    assertEquals("ppc64le", SysUtils.decodeGoSdkArchType("PPC64LE"));
  }

  @Test
  public void decodesArmv6lArchitecture() {
    assertEquals("armv6l", SysUtils.decodeGoSdkArchType("armv6l"));
  }

  @Test
  public void decodesArm64Architecture() {
    assertEquals("arm64", SysUtils.decodeGoSdkArchType("aarch64-arm64"));
  }

  @Test
  public void decodesS390Architecture() {
    assertEquals("s390x", SysUtils.decodeGoSdkArchType("s390"));
  }

  @Test
  public void decodes32BitArchitectureAliases() {
    assertEquals("386", SysUtils.decodeGoSdkArchType("i686"));
  }

  @Test
  public void decodes64BitArchitectureAliases() {
    assertEquals("amd64", SysUtils.decodeGoSdkArchType("x86_64"));
  }

  @Test
  public void returnsNullForUnknownArchitecture() {
    assertNull(SysUtils.decodeGoSdkArchType("mips"));
  }

  @Test
  public void returnsOperatingSystemType() {
    String osType = SysUtils.findGoSdkOsType();
    if (osType != null) {
      assertEquals(true, osType.equals("windows") || osType.equals("freebsd")
          || osType.equals("darwin") || osType.equals("linux"));
    }
  }
}
