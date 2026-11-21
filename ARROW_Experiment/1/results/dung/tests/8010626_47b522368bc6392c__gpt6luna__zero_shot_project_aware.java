package org.molgenis.security.core.runas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
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
  public void principalIsSystemPrincipalSingleton() {
    assertSame(
        SystemSecurityToken.SystemPrincipal.getInstance(),
        SystemSecurityToken.getInstance().getPrincipal());
  }

  @Test
  public void systemPrincipalGetInstanceReturnsSingleton() {
    assertSame(
        SystemSecurityToken.SystemPrincipal.getInstance(),
        SystemSecurityToken.SystemPrincipal.getInstance());
  }

  @Test
  public void tokenIsAuthenticated() {
    assertTrue(SystemSecurityToken.getInstance().isAuthenticated());
  }

  @Test
  public void authoritiesContainSystemRolesInOrder() {
    List<String> authorities =
        Arrays.asList(
            "ROLE_SYSTEM",
            "ROLE_ACL_TAKE_OWNERSHIP",
            "ROLE_ACL_MODIFY_AUDITING",
            "ROLE_ACL_GENERAL_CHANGES");
    List<String> actual = new java.util.ArrayList<>();
    for (GrantedAuthority authority : SystemSecurityToken.getInstance().getAuthorities()) {
      actual.add(authority.getAuthority());
    }
    assertEquals(authorities, actual);
  }
}
