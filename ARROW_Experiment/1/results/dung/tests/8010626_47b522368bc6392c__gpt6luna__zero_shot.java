package org.molgenis.security.core.runas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

public class SystemSecurityToken_ArrowTest {
  @Test
  public void getInstanceReturnsSingleton() {
    assertSame(SystemSecurityToken.getInstance(), SystemSecurityToken.getInstance());
  }

  @Test
  public void credentialsAreNull() {
    assertNull(SystemSecurityToken.getInstance().getCredentials());
  }

  @Test
  public void principalIsSingletonSystemPrincipal() {
    SystemSecurityToken.SystemPrincipal principal =
        (SystemSecurityToken.SystemPrincipal) SystemSecurityToken.getInstance().getPrincipal();
    assertSame(principal, SystemSecurityToken.SystemPrincipal.getInstance());
  }

  @Test
  public void isAuthenticatedReturnsTrue() {
    assertTrue(SystemSecurityToken.getInstance().isAuthenticated());
  }

  @Test
  public void authoritiesContainSystemRolesInOrder() {
    List<String> authorities = Arrays.asList(
        "ROLE_SYSTEM",
        "ROLE_ACL_TAKE_OWNERSHIP",
        "ROLE_ACL_MODIFY_AUDITING",
        "ROLE_ACL_GENERAL_CHANGES");
    List<String> actual = new java.util.ArrayList<String>();
    for (GrantedAuthority authority : SystemSecurityToken.getInstance().getAuthorities()) {
      actual.add(authority.getAuthority());
    }
    assertEquals(authorities, actual);
  }
}
