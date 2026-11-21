package org.zalando.stups.oauth2.spring.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TokenInfoResourceServerTokenServices_ArrowTest {

    @Test
    public void loadAuthenticationReturnsAuthenticationForNullTokenInfoResponse() {
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(new TokenInfoRequestExecutor() {
                    @Override
                    public Map<String, Object> getMap(String accessToken) {
                        return null;
                    }
                });

        OAuth2Authentication authentication = services.loadAuthentication("token");

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
    }

    @Test
    public void loadAuthenticationReturnsAuthenticationForErrorResponse() {
        final Map<String, Object> response = new HashMap<String, Object>();
        response.put("error", "invalid_token");
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(new TokenInfoRequestExecutor() {
                    @Override
                    public Map<String, Object> getMap(String accessToken) {
                        return response;
                    }
                });

        OAuth2Authentication authentication = services.loadAuthentication("token");

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
    }

    @Test(expected = InvalidTokenException.class)
    public void loadAuthenticationRejectsEmptyToken() {
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(new TokenInfoRequestExecutor() {
                    @Override
                    public Map<String, Object> getMap(String accessToken) {
                        return Collections.emptyMap();
                    }
                });

        services.loadAuthentication(" ");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void readAccessTokenIsNotSupported() {
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices(new TokenInfoRequestExecutor() {
                    @Override
                    public Map<String, Object> getMap(String accessToken) {
                        return Collections.emptyMap();
                    }
                });

        services.readAccessToken("token");
    }

    @Test
    public void getAuthenticationExtractorReturnsConfiguredExtractor() {
        AuthenticationExtractor extractor = new DefaultAuthenticationExtractor();
        TokenInfoResourceServerTokenServices services =
                new TokenInfoResourceServerTokenServices("client",
                        extractor,
                        new org.zalando.stups.oauth2.spring.authorization.DefaultUserRolesProvider(),
                        new TokenInfoRequestExecutor() {
                            @Override
                            public Map<String, Object> getMap(String accessToken) {
                                return Collections.emptyMap();
                            }
                        });

        assertSame(extractor, services.getAuthenticationExtractor());
    }
}
