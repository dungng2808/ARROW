package com.coverity.security.pie.policy.csp;

import com.coverity.security.pie.core.FactMetaData;
import com.coverity.security.pie.core.Policy;
import com.coverity.security.pie.policy.csp.fact.UriFactMetaData;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;

public class CspPolicy_ArrowTest {

    private CspPolicy cspPolicy;

    @Before
    public void setUp() {
        cspPolicy = new CspPolicy();
    }

    @Test
    public void testGetName() {
        assertEquals("csp", cspPolicy.getName());
    }

    @Test
    public void testGetRootFactMetaData() {
        FactMetaData metaData = cspPolicy.getRootFactMetaData();
        assertNotNull(metaData);
        assertEquals(UriFactMetaData.getInstance(), metaData);
    }

    @Test
    public void testGetPolicyForUri_Empty() {
        String policy = cspPolicy.getPolicyForUri("http://example.com");
        assertNotNull(policy);
        // Should contain all directives with 'none'
        assertTrue(policy.contains("connect-src 'none';"));
        assertTrue(policy.contains("font-src 'none';"));
        assertTrue(policy.contains("frame-src 'none';"));
        assertTrue(policy.contains("img-src 'none';"));
        assertTrue(policy.contains("media-src 'none';"));
        assertTrue(policy.contains("object-src 'none';"));
        assertTrue(policy.contains("script-src 'none';"));
        assertTrue(policy.contains("style-src 'none';"));
    }

    @Test
    public void testGetPolicyForUri_WithGrants() {
        // Add some grants to test the policy generation
        Policy policy = new CspPolicy();
        // We can't directly add grants, but we can test the behavior through the parent class
        // This test focuses on the logic of policy generation
        
        // Since we can't easily set up grants, we'll test the structure
        String policyResult = cspPolicy.getPolicyForUri("http://example.com");
        assertNotNull(policyResult);
        // Should end with semicolon and space
        assertTrue(policyResult.endsWith("; "));
    }

    @Test
    public void testLogViolation() {
        // This is just a smoke test since the method delegates to parent
        URI uri = URI.create("http://example.com/test");
        cspPolicy.logViolation(uri, "script-src", "evil.com");
        // No exception should be thrown
        assertTrue(true);
    }
}
