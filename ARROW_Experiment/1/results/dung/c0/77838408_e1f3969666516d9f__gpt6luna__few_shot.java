package org.pac4j.async.core.authorization.checker;

import org.junit.Test;
import org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.profile.CommonProfile;

import java.util.ArrayList;
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
        List<CommonProfile> profiles = new ArrayList<>();
        profiles.add(new CommonProfile());

        CompletableFuture<Boolean> result = checker.isAuthorized(null, profiles, "", new HashMap<String, AsyncAuthorizer>());

        assertTrue(result.join());
    }

    @Test
    public void customAuthorizerIsInvokedAndAllows() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        List<CommonProfile> profiles = new ArrayList<>();
        profiles.add(new CommonProfile());
        Map<String, AsyncAuthorizer> authorizers = new HashMap<>();
        authorizers.put("custom", new AsyncAuthorizer<CommonProfile>() {
            @Override
            public CompletableFuture<Boolean> isAuthorized(AsyncWebContext context, List<CommonProfile> profiles) {
                return CompletableFuture.completedFuture(true);
            }
        });

        CompletableFuture<Boolean> result = checker.isAuthorized(null, profiles, "custom", authorizers);

        assertTrue(result.join());
    }

    @Test
    public void customAuthorizerDenialIsReturned() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        List<CommonProfile> profiles = new ArrayList<>();
        profiles.add(new CommonProfile());
        Map<String, AsyncAuthorizer> authorizers = new HashMap<>();
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
    public void builtInAnonymousAuthorizerAllowsAnonymousProfile() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        List<CommonProfile> profiles = new ArrayList<>();
        profiles.add(new CommonProfile());

        CompletableFuture<Boolean> result = checker.isAuthorized(null, profiles, "isAnonymous", new HashMap<String, AsyncAuthorizer>());

        assertTrue(result.join());
    }

    @Test(expected = RuntimeException.class)
    public void emptyProfilesAreRejected() {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        checker.isAuthorized(null, new ArrayList<CommonProfile>(), "", new HashMap<String, AsyncAuthorizer>());
    }
}
