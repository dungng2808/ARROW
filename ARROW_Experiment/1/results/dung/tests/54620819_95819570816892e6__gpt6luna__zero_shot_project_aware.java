package com.igormaznitsa.mvngolang.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
  public void decodes32BitArchitectureNames() {
    assertEquals("386", SysUtils.decodeGoSdkArchType("i686"));
  }

  @Test
  public void decodes64BitArchitectureNames() {
    assertEquals("amd64", SysUtils.decodeGoSdkArchType("x86_64"));
  }

  @Test
  public void returnsNullForUnknownArchitecture() {
    assertNull(SysUtils.decodeGoSdkArchType("riscv64"));
  }

  @Test(expected = NullPointerException.class)
  public void rejectsNullArchitecture() {
    SysUtils.decodeGoSdkArchType(null);
  }

  @Test
  public void returnsSupportedOperatingSystemType() {
    String osType = SysUtils.findGoSdkOsType();
    if (osType != null) {
      assertEquals(true, "windows".equals(osType) || "freebsd".equals(osType)
          || "darwin".equals(osType) || "linux".equals(osType));
    }
  }
}
