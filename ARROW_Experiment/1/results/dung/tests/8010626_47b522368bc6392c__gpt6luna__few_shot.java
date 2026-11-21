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
    SystemSecurityToken token = SystemSecurityToken.getInstance();
    assertNull(token.getCredentials());
  }

  @Test
  public void principalIsSingletonSystemPrincipal() {
    SystemSecurityToken token = SystemSecurityToken.getInstance();
    assertSame(token.getPrincipal(), SystemSecurityToken.SystemPrincipal.getInstance());
    assertSame(SystemSecurityToken.SystemPrincipal.getInstance(),
        SystemSecurityToken.SystemPrincipal.getInstance());
  }

  @Test
  public void tokenIsAuthenticated() {
    SystemSecurityToken token = SystemSecurityToken.getInstance();
    assertTrue(token.isAuthenticated());
  }

  @Test
  public void authoritiesContainSystemRoles() {
    SystemSecurityToken token = SystemSecurityToken.getInstance();
    List<String> authorities = Arrays.asList(
        "ROLE_SYSTEM",
        "ROLE_ACL_TAKE_OWNERSHIP",
        "ROLE_ACL_MODIFY_AUDITING",
        "ROLE_ACL_GENERAL_CHANGES");
    assertEquals(authorities.size(), token.getAuthorities().size());
    for (int i = 0; i < authorities.size(); i++) {
      GrantedAuthority authority = token.getAuthorities().toArray(new GrantedAuthority[0])[i];
      assertEquals(authorities.get(i), authority.getAuthority());
    }
  }
}
