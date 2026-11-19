package org.zalando.stups.oauth2.spring.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

public class TokenInfoResourceServerTokenServices_ArrowTest {

    @Test
    public void loadAuthenticationWithNullResponseReturnsAuthenticationWithoutScopes() {
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(new FixedTokenInfoRequestExecutor(null));

        OAuth2Authentication authentication = services.loadAuthentication("token");

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().isEmpty());
        assertEquals("CLIENT_ID_NOT_NEEDED", authentication.getOAuth2Request().getClientId());
    }

    @Test
    public void loadAuthenticationWithErrorResponseReturnsAuthenticationWithoutScopes() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("error", "invalid_token");
        response.put("error_description", "Token is invalid");
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(new FixedTokenInfoRequestExecutor(response));

        OAuth2Authentication authentication = services.loadAuthentication("token");

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().isEmpty());
    }

    @Test
    public void loadAuthenticationWithErrorAndNoDescriptionReturnsAuthenticationWithoutScopes() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("error", "invalid_token");
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(new FixedTokenInfoRequestExecutor(response));

        OAuth2Authentication authentication = services.loadAuthentication("token");

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().isEmpty());
    }

    @Test
    public void getAuthenticationExtractorReturnsConfiguredExtractor() {
        AuthenticationExtractor extractor = new DefaultAuthenticationExtractor();
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices("client",
                        extractor,
                        new DefaultUserRolesProvider(),
                        new FixedTokenInfoRequestExecutor(Collections.<String, Object>emptyMap()));

        assertSame(extractor, services.getAuthenticationExtractor());
    }

    @Test
    public void readAccessTokenIsUnsupported() {
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(new FixedTokenInfoRequestExecutor(null));

        try {
            services.readAccessToken("token");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            assertEquals("Not supported: read access token", expected.getMessage());
        }
    }

    @Test
    public void loadAuthenticationRejectsNullAccessToken() {
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(new FixedTokenInfoRequestExecutor(null));

        try {
            services.loadAuthentication(null);
            fail("Expected InvalidTokenException");
        } catch (InvalidTokenException expected) {
            assertEquals("'accessToken' should never be null or empty", expected.getMessage());
        }
    }

    @Test
    public void loadAuthenticationRejectsEmptyAccessToken() {
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(new FixedTokenInfoRequestExecutor(null));

        try {
            services.loadAuthentication("");
            fail("Expected InvalidTokenException");
        } catch (InvalidTokenException expected) {
            assertEquals("'accessToken' should never be null or empty", expected.getMessage());
        }
    }

    private static class FixedTokenInfoRequestExecutor extends TokenInfoRequestExecutor {
        private final Map<String, Object> response;

        FixedTokenInfoRequestExecutor(Map<String, Object> response) {
            super(null, null);
            this.response = response;
        }

        @Override
        public Map<String, Object> getMap(String accessToken) {
            return response;
        }
    }
}
