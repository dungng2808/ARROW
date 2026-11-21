package org.zalando.stups.oauth2.spring.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import org.zalando.stups.oauth2.spring.authorization.DefaultUserRolesProvider;
import org.zalando.stups.oauth2.spring.authorization.UserRolesProvider;

public class TokenInfoResourceServerTokenServices_ArrowTest {

    private TokenInfoResourceServerTokenServices services;
    private TokenInfoRequestExecutor executor;
    private AuthenticationExtractor extractor;
    private UserRolesProvider rolesProvider;

    @Before
    public void setUp() {
        executor = mock(TokenInfoRequestExecutor.class);
        extractor = mock(AuthenticationExtractor.class);
        rolesProvider = mock(UserRolesProvider.class);
    }

    @Test
    public void testConstructorWithTokenInfoEndpointUrlOnly() {
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices("http://example.com/tokeninfo");
        assertNotNull(services);
    }

    @Test
    public void testConstructorWithAllParameters() {
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "http://example.com/tokeninfo", "client123", extractor, rolesProvider, new RestTemplate());
        assertNotNull(services);
    }

    @Test
    public void testLoadAuthenticationWithNullAccessToken() throws AuthenticationException, InvalidTokenException {
        services = new TokenInfoResourceServerTokenServices(executor);
        try {
            services.loadAuthentication(null);
            fail("Expected InvalidTokenException");
        } catch (InvalidTokenException e) {
            // Expected
        }
    }

    @Test
    public void testLoadAuthenticationWithEmptyAccessToken() throws AuthenticationException, InvalidTokenException {
        services = new TokenInfoResourceServerTokenServices(executor);
        try {
            services.loadAuthentication("");
            fail("Expected InvalidTokenException");
        } catch (InvalidTokenException e) {
            // Expected
        }
    }

    @Test
    public void testLoadAuthenticationWithNullMap() throws AuthenticationException, InvalidTokenException {
        services = new TokenInfoResourceServerTokenServices(executor);
        when(executor.getMap("token123")).thenReturn(null);
        
        OAuth2Authentication result = services.loadAuthentication("token123");
        assertNotNull(result);
    }

    @Test
    public void testLoadAuthenticationWithErrorInMap() throws AuthenticationException, InvalidTokenException {
        services = new TokenInfoResourceServerTokenServices(executor);
        Map<String, Object> map = Collections.singletonMap("error", "invalid_token");
        when(executor.getMap("token123")).thenReturn(map);
        
        OAuth2Authentication result = services.loadAuthentication("token123");
        assertNotNull(result);
    }

    @Test
    public void testLoadAuthenticationWithValidToken() throws AuthenticationException, InvalidTokenException {
        services = new TokenInfoResourceServerTokenServices(executor, extractor, rolesProvider);
        Map<String, Object> map = Collections.singletonMap("scope", "read");
        when(executor.getMap("token123")).thenReturn(map);
        OAuth2Authentication expectedAuth = mock(OAuth2Authentication.class);
        when(extractor.extractAuthentication(map, "CLIENT_ID_NOT_NEEDED", rolesProvider)).thenReturn(expectedAuth);
        
        OAuth2Authentication result = services.loadAuthentication("token123");
        assertEquals(expectedAuth, result);
    }

    @Test
    public void testGetAuthenticationExtractor() {
        services = new TokenInfoResourceServerTokenServices(executor);
        AuthenticationExtractor extractor = services.getAuthenticationExtractor();
        assertNotNull(extractor);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testReadAccessToken() {
        services = new TokenInfoResourceServerTokenServices(executor);
        services.readAccessToken("token123");
    }
}
