package org.molgenis.security.core.runas;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.Assert.*;

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
        assertEquals(SystemSecurityToken.SystemPrincipal.getInstance(), principal);
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
        
        // Check that all expected roles are present
        boolean hasRoleSystem = false;
        boolean hasRoleTakeOwnership = false;
        boolean hasRoleModifyAuditing = false;
        boolean hasRoleGeneralChanges = false;
        
        for (SimpleGrantedAuthority authority : authorities) {
            switch (authority.getAuthority()) {
                case "ROLE_SYSTEM":
                    hasRoleSystem = true;
                    break;
                case "ROLE_ACL_TAKE_OWNERSHIP":
                    hasRoleTakeOwnership = true;
                    break;
                case "ROLE_ACL_MODIFY_AUDITING":
                    hasRoleModifyAuditing = true;
                    break;
                case "ROLE_ACL_GENERAL_CHANGES":
                    hasRoleGeneralChanges = true;
                    break;
            }
        }
        
        assertTrue(hasRoleSystem);
        assertTrue(hasRoleTakeOwnership);
        assertTrue(hasRoleModifyAuditing);
        assertTrue(hasRoleGeneralChanges);
    }

    @Test
    public void testSystemPrincipalGetInstance() {
        SystemSecurityToken.SystemPrincipal principal1 = SystemSecurityToken.SystemPrincipal.getInstance();
        SystemSecurityToken.SystemPrincipal principal2 = SystemSecurityToken.SystemPrincipal.getInstance();
        assertSame(principal1, principal2);
    }
}
