package org.pac4j.async.core.authorization.checker;

import org.junit.Test;
import org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.authorization.authorizer.IsAnonymousAuthorizer;
import org.pac4j.core.profile.CommonProfile;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultAsyncAuthorizationChecker_ArrowTest {

    @Test
    public void testIsAuthorizedWithEmptyAuthorizerNames() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));
        Map<String, AsyncAuthorizer> authorizersMap = new HashMap<>();

        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "", authorizersMap);
        assertTrue(result.join());
    }

    @Test
    public void testIsAuthorizedWithNullAuthorizerNames() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));
        Map<String, AsyncAuthorizer> authorizersMap = new HashMap<>();

        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, null, authorizersMap);
        assertTrue(result.join());
    }

    @Test
    public void testIsAuthorizedWithSingleAuthorizerName() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));
        Map<String, AsyncAuthorizer> authorizersMap = new HashMap<>();
        AsyncAuthorizer mockAuthorizer = mock(AsyncAuthorizer.class);
        when(mockAuthorizer.isAuthorized(any(), any())).thenReturn(CompletableFuture.completedFuture(true));
        authorizersMap.put("custom", mockAuthorizer);

        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "custom", authorizersMap);
        assertTrue(result.join());
    }

    @Test
    public void testIsAuthorizedWithMultipleAuthorizerNames() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));
        Map<String, AsyncAuthorizer> authorizersMap = new HashMap<>();
        AsyncAuthorizer mockAuthorizer1 = mock(AsyncAuthorizer.class);
        AsyncAuthorizer mockAuthorizer2 = mock(AsyncAuthorizer.class);
        when(mockAuthorizer1.isAuthorized(any(), any())).thenReturn(CompletableFuture.completedFuture(true));
        when(mockAuthorizer2.isAuthorized(any(), any())).thenReturn(CompletableFuture.completedFuture(true));
        authorizersMap.put("custom1", mockAuthorizer1);
        authorizersMap.put("custom2", mockAuthorizer2);

        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "custom1, custom2", authorizersMap);
        assertTrue(result.join());
    }

    @Test
    public void testIsAuthorizedWithBuiltInAuthorizerNames() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));

        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "isAnonymous", new HashMap<>());
        assertTrue(result.join());
    }

    @Test
    public void testIsAuthorizedWithInvalidAuthorizerName() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));
        Map<String, AsyncAuthorizer> authorizersMap = new HashMap<>();

        try {
            checker.isAuthorized(context, profiles, "invalid", authorizersMap);
            fail("Should have thrown an exception");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testIsAuthorizedWithEmptyProfiles() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = new ArrayList<>();
        Map<String, AsyncAuthorizer> authorizersMap = new HashMap<>();

        try {
            checker.isAuthorized(context, profiles, "", authorizersMap);
            fail("Should have thrown an exception");
        } catch (AssertionError e) {
            // Expected
        }
    }

    @Test
    public void testIsAuthorizedWithNullProfiles() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = null;
        Map<String, AsyncAuthorizer> authorizersMap = new HashMap<>();

        try {
            checker.isAuthorized(context, profiles, "", authorizersMap);
            fail("Should have thrown an exception");
        } catch (AssertionError e) {
            // Expected
        }
    }
}
