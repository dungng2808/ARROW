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

public class DefaultAsyncAuthorizationChecker_ArrowTest {

    @Test
    public void emptyAuthorizerNamesAllowsNonEmptyProfiles() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        List<CommonProfile> profiles = Collections.singletonList(new CommonProfile());

        CompletableFuture<Boolean> result = checker.isAuthorized(null, profiles, "", new HashMap<String, AsyncAuthorizer>());

        assertTrue(result.join());
    }

    @Test
    public void customAuthorizerResultIsReturned() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        List<CommonProfile> profiles = Collections.singletonList(new CommonProfile());
        Map<String, AsyncAuthorizer> authorizers = new HashMap<String, AsyncAuthorizer>();
        authorizers.put("custom", new AsyncAuthorizer<CommonProfile>() {
            @Override
            public CompletableFuture<Boolean> isAuthorized(AsyncWebContext context, List<CommonProfile> profiles) {
                return CompletableFuture.completedFuture(false);
            }
        });

        CompletableFuture<Boolean> result = checker.isAuthorized(null, profiles, "custom", authorizers);

        assertFalse(result.join());
    }

    @Test
    public void customAuthorizerNameIsMatchedIgnoringCaseAndWhitespace() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        List<CommonProfile> profiles = Collections.singletonList(new CommonProfile());
        Map<String, AsyncAuthorizer> authorizers = new HashMap<String, AsyncAuthorizer>();
        authorizers.put(" custom ", new AsyncAuthorizer<CommonProfile>() {
            @Override
            public CompletableFuture<Boolean> isAuthorized(AsyncWebContext context, List<CommonProfile> profiles) {
                return CompletableFuture.completedFuture(true);
            }
        });

        CompletableFuture<Boolean> result = checker.isAuthorized(null, profiles, "CUSTOM", authorizers);

        assertTrue(result.join());
    }

    @Test
    public void multipleCustomAuthorizersShortCircuitOnFalse() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        List<CommonProfile> profiles = Collections.singletonList(new CommonProfile());
        final int[] calls = new int[] {0};
        Map<String, AsyncAuthorizer> authorizers = new HashMap<String, AsyncAuthorizer>();
        authorizers.put("first", new AsyncAuthorizer<CommonProfile>() {
            @Override
            public CompletableFuture<Boolean> isAuthorized(AsyncWebContext context, List<CommonProfile> profiles) {
                calls[0]++;
                return CompletableFuture.completedFuture(false);
            }
        });
        authorizers.put("second", new AsyncAuthorizer<CommonProfile>() {
            @Override
            public CompletableFuture<Boolean> isAuthorized(AsyncWebContext context, List<CommonProfile> profiles) {
                calls[0]++;
                return CompletableFuture.completedFuture(true);
            }
        });

        CompletableFuture<Boolean> result = checker.isAuthorized(null, profiles, "first,second", authorizers);

        assertFalse(result.join());
        assertEquals(1, calls[0]);
    }

    @Test
    public void whitespaceOnlyAuthorizerNamesAllowsNonEmptyProfiles() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        List<CommonProfile> profiles = Collections.singletonList(new CommonProfile());

        CompletableFuture<Boolean> result = checker.isAuthorized(null, profiles, "   ", new HashMap<String, AsyncAuthorizer>());

        assertTrue(result.join());
    }

    @Test(expected = RuntimeException.class)
    public void emptyProfilesAreRejected() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        checker.isAuthorized(null, new ArrayList<CommonProfile>(), "", new HashMap<String, AsyncAuthorizer>());
    }
}
