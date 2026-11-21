package org.zalando.stups.oauth2.spring.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenInfoResourceServerTokenServices_ArrowTest {

    @Test
    public void loadAuthenticationReturnsAuthenticationWithoutScopesForNullResponse() {
        TokenInfoRequestExecutor executor = mock(TokenInfoRequestExecutor.class);
        when(executor.getMap("token")).thenReturn(null);
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(executor);

        OAuth2Authentication authentication = services.loadAuthentication("token");

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(Collections.<String>emptySet(), authentication.getOAuth2Request().getScope());
    }

    @Test
    public void loadAuthenticationReturnsAuthenticationWithoutScopesForErrorResponse() {
        TokenInfoRequestExecutor executor = mock(TokenInfoRequestExecutor.class);
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("error", "invalid_token");
        when(executor.getMap("token")).thenReturn(response);
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(executor);

        OAuth2Authentication authentication = services.loadAuthentication("token");

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(Collections.<String>emptySet(), authentication.getOAuth2Request().getScope());
    }

    @Test
    public void getAuthenticationExtractorReturnsProvidedExtractor() {
        AuthenticationExtractor extractor = mock(AuthenticationExtractor.class);
        TokenInfoRequestExecutor executor = mock(TokenInfoRequestExecutor.class);
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices("client", extractor,
                        mock(org.zalando.stups.oauth2.spring.authorization.UserRolesProvider.class), executor);

        assertSame(extractor, services.getAuthenticationExtractor());
    }

    @Test(expected = InvalidTokenException.class)
    public void loadAuthenticationRejectsEmptyAccessToken() {
        TokenInfoRequestExecutor executor = mock(TokenInfoRequestExecutor.class);
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(executor);

        services.loadAuthentication("");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void readAccessTokenIsNotSupported() {
        TokenInfoRequestExecutor executor = mock(TokenInfoRequestExecutor.class);
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(executor);

        services.readAccessToken("token");
    }
}
