package org.molgenis.security.core.runas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.molgenis.security.core.runas.SystemSecurityToken.SystemPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SystemSecurityToken_ArrowTest {

  @Test
  public void testGetInstance() {
    SystemSecurityToken token1 = SystemSecurityToken.getInstance();
    SystemSecurityToken token2 = SystemSecurityToken.getInstance();
    assertSame(token1, token2);
  }

  @Test
  public void testGetCredentials() {
    SystemSecurityToken token = SystemSecurityToken.getInstance();
    assertNull(token.getCredentials());
  }

  @Test
  public void testGetPrincipal() {
    SystemSecurityToken token = SystemSecurityToken.getInstance();
    Object principal = token.getPrincipal();
    assertNotNull(principal);
    assertTrue(principal instanceof SystemPrincipal);
    assertSame(SystemPrincipal.getInstance(), principal);
  }

  @Test
  public void testIsAuthenticated() {
    SystemSecurityToken token = SystemSecurityToken.getInstance();
    assertTrue(token.isAuthenticated());
  }

  @Test
  public void testAuthorities() {
    SystemSecurityToken token = SystemSecurityToken.getInstance();
    List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>) token.getAuthorities();
    assertEquals(4, authorities.size());
    assertEquals("ROLE_SYSTEM", authorities.get(0).getAuthority());
    assertEquals("ROLE_ACL_TAKE_OWNERSHIP", authorities.get(1).getAuthority());
    assertEquals("ROLE_ACL_MODIFY_AUDITING", authorities.get(2).getAuthority());
    assertEquals("ROLE_ACL_GENERAL_CHANGES", authorities.get(3).getAuthority());
  }

  @Test
  public void testSystemPrincipalGetInstance() {
    SystemPrincipal principal1 = SystemPrincipal.getInstance();
    SystemPrincipal principal2 = SystemPrincipal.getInstance();
    assertSame(principal1, principal2);
  }
}
