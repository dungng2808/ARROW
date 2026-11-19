package org.zalando.stups.oauth2.spring.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    public void testConstructorWithAllParameters() {
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "http://example.com/tokeninfo", "test-client", extractor, rolesProvider, new RestTemplate());
        
        assertNotNull(services);
    }

    @Test
    public void testConstructorWithTokenInfoRequestExecutor() {
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(executor);
        
        assertNotNull(services);
    }

    @Test
    public void testLoadAuthenticationWithNullAccessToken() throws AuthenticationException, InvalidTokenException {
        services = new TokenInfoResourceServerTokenServices("http://example.com/tokeninfo", "test-client", 
                extractor, rolesProvider, new RestTemplate());
        
        try {
            services.loadAuthentication(null);
            fail("Expected InvalidTokenException");
        } catch (InvalidTokenException e) {
            // Expected
        }
    }

    @Test
    public void testLoadAuthenticationWithEmptyAccessToken() throws AuthenticationException, InvalidTokenException {
        services = new TokenInfoResourceServerTokenServices("http://example.com/tokeninfo", "test-client", 
                extractor, rolesProvider, new RestTemplate());
        
        try {
            services.loadAuthentication("");
            fail("Expected InvalidTokenException");
        } catch (InvalidTokenException e) {
            // Expected
        }
    }

    @Test
    public void testLoadAuthenticationReturnsNullFromExecutor() throws AuthenticationException, InvalidTokenException {
        services = new TokenInfoResourceServerTokenServices("http://example.com/tokeninfo", "test-client", 
                extractor, rolesProvider, new RestTemplate());
        
        when(executor.getMap("test-token")).thenReturn(null);
        
        OAuth2Authentication result = services.loadAuthentication("test-token");
        
        assertNotNull(result);
    }

    @Test
    public void testLoadAuthenticationWithErrorInResponse() throws AuthenticationException, InvalidTokenException {
        services = new TokenInfoResourceServerTokenServices("http://example.com/tokeninfo", "test-client", 
                extractor, rolesProvider, new RestTemplate());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "invalid_token");
        errorResponse.put("error_description", "Invalid token");
        
        when(executor.getMap("test-token")).thenReturn(errorResponse);
        
        OAuth2Authentication result = services.loadAuthentication("test-token");
        
        assertNotNull(result);
    }

    @Test
    public void testLoadAuthenticationSuccess() throws AuthenticationException, InvalidTokenException {
        services = new TokenInfoResourceServerTokenServices("http://example.com/tokeninfo", "test-client", 
                extractor, rolesProvider, new RestTemplate());
        
        Map<String, Object> validResponse = new HashMap<>();
        validResponse.put("user_name", "testuser");
        validResponse.put("scope", Collections.singletonList("read"));
        
        when(executor.getMap("test-token")).thenReturn(validResponse);
        when(extractor.extractAuthentication(validResponse, "test-client", rolesProvider)).thenReturn(mock(OAuth2Authentication.class));
        
        OAuth2Authentication result = services.loadAuthentication("test-token");
        
        assertNotNull(result);
    }

    @Test
    public void testGetAuthenticationExtractor() {
        services = new TokenInfoResourceServerTokenServices("http://example.com/tokeninfo", "test-client", 
                extractor, rolesProvider, new RestTemplate());
        
        assertEquals(extractor, services.getAuthenticationExtractor());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testReadAccessTokenUnsupported() {
        services = new TokenInfoResourceServerTokenServices("http://example.com/tokeninfo", "test-client", 
                extractor, rolesProvider, new RestTemplate());
        
        services.readAccessToken("test-token");
    }

    @Test
    public void testConstructorWithDefaultValues() {
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "http://example.com/tokeninfo");
        
        assertNotNull(services);
    }

    @Test
    public void testConstructorWithClientIdAndExtractor() {
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "http://example.com/tokeninfo", "test-client", extractor);
        
        assertNotNull(services);
    }

    @Test
    public void testConstructorWithClientIdAndUserRolesProvider() {
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "http://example.com/tokeninfo", "test-client", rolesProvider);
        
        assertNotNull(services);
    }

    @Test
    public void testConstructorWithExtractorOnly() {
        TokenInfoResourceServerTokenServices services = new TokenInfoResourceServerTokenServices(
                "http://example.com/tokeninfo", extractor);
        
        assertNotNull(services);
    }

    @Test
    public void testGetMap() {
        services = new TokenInfoResourceServerTokenServices("http://example.com/tokeninfo", "test-client", 
                extractor, rolesProvider, new RestTemplate());
        
        Map<String, Object> expected = new HashMap<>();
        when(executor.getMap("test-token")).thenReturn(expected);
        
        Map<String, Object> result = services.getMap("test-token");
        
        assertEquals(expected, result);
    }
}
