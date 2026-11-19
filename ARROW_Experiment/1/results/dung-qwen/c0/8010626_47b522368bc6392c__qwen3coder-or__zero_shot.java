package org.molgenis.security.core.runas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.junit.Test;
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
    assertSame(SystemSecurityToken.SystemPrincipal.getInstance(), principal);
  }

  @Test
  public void testIsAuthenticated() {
    SystemSecurityToken token = SystemSecurityToken.getInstance();
    assertTrue(token.isAuthenticated());
  }

  @Test
  public void testAuthorities() {
    SystemSecurityToken token = SystemSecurityToken.getInstance();
    Collection<? extends org.springframework.security.core.GrantedAuthority> authorities =
        token.getAuthorities();
    assertEquals(4, authorities.size());

    ImmutableList<SimpleGrantedAuthority> expectedAuthorities =
        ImmutableList.of(
            new SimpleGrantedAuthority(org.molgenis.security.core.utils.SecurityUtils.ROLE_SYSTEM),
            new SimpleGrantedAuthority(
                org.molgenis.security.core.utils.SecurityUtils.ROLE_ACL_TAKE_OWNERSHIP),
            new SimpleGrantedAuthority(
                org.molgenis.security.core.utils.SecurityUtils.ROLE_ACL_MODIFY_AUDITING),
            new SimpleGrantedAuthority(
                org.molgenis.security.core.utils.SecurityUtils.ROLE_ACL_GENERAL_CHANGES));

    assertEquals(expectedAuthorities, authorities);
  }

  @Test
  public void testSystemPrincipalGetInstance() {
    SystemSecurityToken.SystemPrincipal principal1 =
        SystemSecurityToken.SystemPrincipal.getInstance();
    SystemSecurityToken.SystemPrincipal principal2 =
        SystemSecurityToken.SystemPrincipal.getInstance();
    assertSame(principal1, principal2);
  }
}
