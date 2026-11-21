package org.zalando.stups.oauth2.spring.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.client.RestTemplate;

import org.zalando.stups.oauth2.spring.authorization.DefaultUserRolesProvider;
import org.zalando.stups.oauth2.spring.authorization.UserRolesProvider;
import org.zalando.stups.oauth2.spring.server.TokenInfoResourceServerTokenServices;
import org.zalando.stups.oauth2.spring.server.TokenInfoRequestExecutor;

public class TokenInfoResourceServerTokenServices_ArrowTest {

    private TokenInfoRequestExecutor executor;
    private UserRolesProvider rolesProvider;
    private AuthenticationExtractor extractor;

    @Before
    public void setUp() {
        executor = mock(TokenInfoRequestExecutor.class);
        rolesProvider = mock(UserRolesProvider.class);
        extractor = mock(AuthenticationExtractor.class);
    }

    @Test
    public void testConstructorWithAllParameters() {
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "client1", extractor, rolesProvider, executor);
        
        assertNotNull(services);
    }

    @Test
    public void testLoadAuthenticationWithValidToken() throws Exception {
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("user_name", "testuser");
        tokenInfo.put("scope", "read write");
        
        when(executor.getMap("valid-token")).thenReturn(tokenInfo);
        
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(null, null, Collections.<GrantedAuthority>emptyList());
        OAuth2Request request = new OAuth2Request(null, "client1", null, true, Collections.<String>emptySet(), null, null, null, null);
        OAuth2Authentication expectedAuth = new OAuth2Authentication(request, token);
        
        when(extractor.extractAuthentication(tokenInfo, "client1", rolesProvider)).thenReturn(expectedAuth);
        
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "client1", extractor, rolesProvider, executor);
        
        OAuth2Authentication result = services.loadAuthentication("valid-token");
        
        assertEquals(expectedAuth, result);
    }

    @Test(expected = InvalidTokenException.class)
    public void testLoadAuthenticationWithEmptyToken() throws Exception {
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "client1", extractor, rolesProvider, executor);
        
        services.loadAuthentication("");
    }

    @Test
    public void testLoadAuthenticationWithNullTokenInfo() throws Exception {
        when(executor.getMap("valid-token")).thenReturn(null);
        
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "client1", extractor, rolesProvider, executor);
        
        OAuth2Authentication result = services.loadAuthentication("valid-token");
        
        assertNotNull(result);
    }

    @Test
    public void testLoadAuthenticationWithErrorInTokenInfo() throws Exception {
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("error", "invalid_token");
        tokenInfo.put("error_description", "Invalid token");
        
        when(executor.getMap("valid-token")).thenReturn(tokenInfo);
        
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "client1", extractor, rolesProvider, executor);
        
        OAuth2Authentication result = services.loadAuthentication("valid-token");
        
        assertNotNull(result);
    }

    @Test
    public void testReadAccessTokenUnsupported() {
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "client1", extractor, rolesProvider, executor);
        
        try {
            services.readAccessToken("any-token");
            // Should not reach here
        } catch (UnsupportedOperationException e) {
            assertEquals("Not supported: read access token", e.getMessage());
        }
    }

    @Test
    public void testGetAuthenticationExtractor() {
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "client1", extractor, rolesProvider, executor);
        
        assertEquals(extractor, services.getAuthenticationExtractor());
    }
}
