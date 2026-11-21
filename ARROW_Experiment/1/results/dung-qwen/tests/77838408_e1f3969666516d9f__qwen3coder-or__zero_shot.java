package org.pac4j.async.core.authorization.checker;

import org.junit.Test;
import org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.authorization.authorizer.IsAnonymousAuthorizer;
import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.authorization.authorizer.IsFullyAuthenticatedAuthorizer;
import org.pac4j.core.authorization.authorizer.IsRememberedAuthorizer;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultAsyncAuthorizationChecker_ArrowTest {

    @Test
    public void testIsAuthorized_EmptyAuthorizers() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));
        
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_SingleAuthorizer() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));
        
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "isAnonymous", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_MultipleAuthorizers() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));
        
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "isAnonymous,isAuthenticated", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_CustomAuthorizer() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));
        
        AsyncAuthorizer<CommonProfile> customAuthorizer = mock(AsyncAuthorizer.class);
        when(customAuthorizer.isAuthorized(any(), any())).thenReturn(CompletableFuture.completedFuture(true));
        
        Map<String, AsyncAuthorizer> authorizersMap = new HashMap<>();
        authorizersMap.put("custom", customAuthorizer);
        
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "custom", authorizersMap);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_InvalidAuthorizerName() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));
        
        Map<String, AsyncAuthorizer> authorizersMap = new HashMap<>();
        
        try {
            checker.isAuthorized(context, profiles, "invalidAuthorizer", authorizersMap);
            fail("Expected exception was not thrown");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testIsAuthorized_NullProfiles() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        
        try {
            checker.isAuthorized(context, null, "isAnonymous", null);
            fail("Expected exception was not thrown");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testIsAuthorized_EmptyProfiles() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        
        try {
            checker.isAuthorized(context, new ArrayList<>(), "isAnonymous", null);
            fail("Expected exception was not thrown");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testIsAuthorized_WithSpecialAuthorizerNames() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));
        
        // Test securityheaders
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "securityheaders", null);
        assertTrue(result.get());
        
        // Test csrf
        result = checker.isAuthorized(context, profiles, "csrf", null);
        assertTrue(result.get());
        
        // Test allowAjaxRequests
        result = checker.isAuthorized(context, profiles, "allowAjaxRequests", null);
        assertTrue(result.get());
    }

    @Test
    public void testIsAuthorized_NonBlockingAuthorizers() throws Exception {
        DefaultAsyncAuthorizationChecker checker = new DefaultAsyncAuthorizationChecker();
        AsyncWebContext context = mock(AsyncWebContext.class);
        List<CommonProfile> profiles = Arrays.asList(mock(CommonProfile.class));
        
        // Test hsts
        CompletableFuture<Boolean> result = checker.isAuthorized(context, profiles, "hsts", null);
        assertTrue(result.get());
        
        // Test nosniff
        result = checker.isAuthorized(context, profiles, "nosniff", null);
        assertTrue(result.get());
        
        // Test noframe
        result = checker.isAuthorized(context, profiles, "noframe", null);
        assertTrue(result.get());
        
        // Test xssprotection
        result = checker.isAuthorized(context, profiles, "xssprotection", null);
        assertTrue(result.get());
        
        // Test nocache
        result = checker.isAuthorized(context, profiles, "nocache", null);
        assertTrue(result.get());
    }
}
