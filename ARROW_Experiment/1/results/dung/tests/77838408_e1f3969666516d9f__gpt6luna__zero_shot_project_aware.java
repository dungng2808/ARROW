package org.pac4j.async.core.authorization.checker;

import org.junit.Test;
import org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.profile.CommonProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultAsyncAuthorizationChecker_ArrowTest {

    @Test
    public void emptyAuthorizerNamesAllowsNonEmptyProfiles() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Collections.singletonList(new CommonProfile());

        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "", new HashMap<String, AsyncAuthorizer>());

        assertTrue(result.join());
    }

    @Test
    public void namedAuthorizerIsInvokedAndAllows() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Collections.singletonList(new CommonProfile());
        AsyncAuthorizer<CommonProfile> authorizer = mock(AsyncAuthorizer.class);
        when(authorizer.isAuthorized(context, profiles)).thenReturn(CompletableFuture.completedFuture(true));
        Map<String, AsyncAuthorizer> authorizers = new HashMap<String, AsyncAuthorizer>();
        authorizers.put("custom", authorizer);

        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, " custom ", authorizers);

        assertTrue(result.join());
    }

    @Test
    public void namedAuthorizerCanDeny() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Collections.singletonList(new CommonProfile());
        AsyncAuthorizer<CommonProfile> authorizer = mock(AsyncAuthorizer.class);
        when(authorizer.isAuthorized(context, profiles)).thenReturn(CompletableFuture.completedFuture(false));
        Map<String, AsyncAuthorizer> authorizers = new HashMap<String, AsyncAuthorizer>();
        authorizers.put("custom", authorizer);

        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "custom", authorizers);

        assertFalse(result.join());
    }

    @Test
    public void multipleNamedAuthorizersAreCheckedInOrderAndShortCircuit() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Collections.singletonList(new CommonProfile());
        AsyncAuthorizer<CommonProfile> denying = mock(AsyncAuthorizer.class);
        AsyncAuthorizer<CommonProfile> allowing = mock(AsyncAuthorizer.class);
        when(denying.isAuthorized(context, profiles)).thenReturn(CompletableFuture.completedFuture(false));
        when(allowing.isAuthorized(context, profiles)).thenReturn(CompletableFuture.completedFuture(true));
        Map<String, AsyncAuthorizer> authorizers = new HashMap<String, AsyncAuthorizer>();
        authorizers.put("first", denying);
        authorizers.put("second", allowing);

        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "first,second", authorizers);

        assertFalse(result.join());
    }

    @Test
    public void builtInAuthorizerNameIsCaseInsensitive() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Collections.singletonList(new CommonProfile());

        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "ISANONYMOUS", new HashMap<String, AsyncAuthorizer>());

        assertTrue(result.join());
    }

    @Test
    public void nullProfilesAreRejected() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();

        try {
            checker.isAuthorized(mock(AsyncWebContext.class), null, "", new HashMap<String, AsyncAuthorizer>());
        } catch (Exception expected) {
            assertTrue(expected instanceof RuntimeException);
            return;
        }
        throw new AssertionError("Expected an exception");
    }

    @Test
    public void emptyProfilesAreRejected() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();

        try {
            checker.isAuthorized(mock(AsyncWebContext.class), new ArrayList<CommonProfile>(), "", new HashMap<String, AsyncAuthorizer>());
        } catch (Exception expected) {
            assertTrue(expected instanceof RuntimeException);
            return;
        }
        throw new AssertionError("Expected an exception");
    }
}
