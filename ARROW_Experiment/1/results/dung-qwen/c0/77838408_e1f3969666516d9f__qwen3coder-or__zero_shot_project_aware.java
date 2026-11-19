package org.pac4j.async.core.authorization.checker;

import org.junit.Test;
import org.junit.Before;
import org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.authorization.authorizer.IsAnonymousAuthorizer;
import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.authorization.authorizer.IsFullyAuthenticatedAuthorizer;
import org.pac4j.core.authorization.authorizer.IsRememberedAuthorizer;
import org.pac4j.core.profile.CommonProfile;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultAsyncAuthorizationChecker_ArrowTest {

    private DefaultAsyncAuthorizationChecker checker;
    private AsyncWebContext context;
    private List<CommonProfile> profiles;

    @Before
    public void setUp() {
        checker = new DefaultAsyncAuthorizationChecker();
        context = mock(AsyncWebContext.class);
        profiles = new ArrayList<>();
        profiles.add(mock(CommonProfile.class));
    }

    @Test
    public void testIsAuthorized_EmptyAuthorizersList() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, new ArrayList<>());
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_NullAuthorizerNames() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, (String) null, null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_EmptyAuthorizerNames() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_IsAnonymous() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "isAnonymous", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_IsAuthenticated() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "isAuthenticated", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_IsFullyAuthenticated() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "isFullyAuthenticated", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_IsRemembered() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "isRemembered", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_Hsts() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "hsts", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_Nosniff() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "nosniff", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_Noframe() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "noframe", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_Xssprotection() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "xssprotection", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_Nocache() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "nocache", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_SecurityHeaders() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "securityheaders", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_CsrfToken() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "csrfToken", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_CsrfCheck() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "csrfCheck", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_Csrf() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "csrf", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizerName_AllowAjaxRequests() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "allowAjaxRequests", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_MultipleAuthorizerNames() throws Exception {
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "isAnonymous,isAuthenticated", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_CustomAuthorizer() throws Exception {
        AsyncAuthorizer<CommonProfile> customAuthorizer = mock(AsyncAuthorizer.class);
        when(customAuthorizer.isAuthorized(any(AsyncWebContext.class), any(List.class))).thenReturn(CompletableFuture.completedFuture(true));

        Map<String, AsyncAuthorizer> authorizersMap = new HashMap<>();
        authorizersMap.put("custom", customAuthorizer);

        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "custom", authorizersMap);
        assertTrue(result.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsAuthorized_InvalidCustomAuthorizerName() throws Exception {
        Map<String, AsyncAuthorizer> authorizersMap = new HashMap<>();
        checker.isAuthorized(context, profiles, "invalid", authorizersMap);
    }

    @Test
    public void testIsAuthorized_WithNullProfiles() throws Exception {
        try {
            checker.isAuthorized(context, null, "isAnonymous", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testIsAuthorized_WithEmptyProfiles() throws Exception {
        try {
            checker.isAuthorized(context, new ArrayList<>(), "isAnonymous", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}
