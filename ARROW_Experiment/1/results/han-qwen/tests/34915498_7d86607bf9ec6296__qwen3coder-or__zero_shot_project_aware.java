package com.coverity.security.pie.policy.csp;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.coverity.security.pie.core.FactMetaData;
import com.coverity.security.pie.core.Policy;
import com.coverity.security.pie.policy.csp.fact.UriFactMetaData;

public class CspPolicy_ArrowTest {
    
    private CspPolicy policy;
    
    @BeforeMethod
    public void setUp() {
        policy = new CspPolicy();
    }
    
    @Test
    public void testGetName() {
        Assert.assertEquals(policy.getName(), "csp");
    }
    
    @Test
    public void testGetRootFactMetaData() {
        FactMetaData metaData = policy.getRootFactMetaData();
        Assert.assertNotNull(metaData);
        Assert.assertEquals(metaData, UriFactMetaData.getInstance());
    }
    
    @Test
    public void testGetPolicyForUri_EmptyGrants() {
        // Test with no grants - should return default policies with 'none'
        String result = policy.getPolicyForUri("/test");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("connect-src 'none';"));
        Assert.assertTrue(result.contains("font-src 'none';"));
        Assert.assertTrue(result.contains("frame-src 'none';"));
        Assert.assertTrue(result.contains("img-src 'none';"));
        Assert.assertTrue(result.contains("media-src 'none';"));
        Assert.assertTrue(result.contains("object-src 'none';"));
        Assert.assertTrue(result.contains("script-src 'none';"));
        Assert.assertTrue(result.contains("style-src 'none';"));
    }
    
    @Test
    public void testGetPolicyForUri_SingleDirective() {
        // Mock grants to simulate a single directive
        List<String[]> grants = new ArrayList<>();
        grants.add(new String[]{"allow", "img-src", "https://example.com"});
        grants.add(new String[]{"allow", "script-src", "https://cdn.example.com"});
        
        // We need to override the getGrants method to return our test data
        // Since we can't modify the class, we'll test the logic by creating a subclass
        CspPolicy testPolicy = new CspPolicy() {
            @Override
            public Collection<String[]> getGrants(String uri, String fact, String value) {
                return grants;
            }
        };
        
        String result = testPolicy.getPolicyForUri("/test");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("connect-src 'none';"));
        Assert.assertTrue(result.contains("font-src 'none';"));
        Assert.assertTrue(result.contains("frame-src 'none';"));
        Assert.assertTrue(result.contains("img-src https://example.com;"));
        Assert.assertTrue(result.contains("media-src 'none';"));
        Assert.assertTrue(result.contains("object-src 'none';"));
        Assert.assertTrue(result.contains("script-src https://cdn.example.com;"));
        Assert.assertTrue(result.contains("style-src 'none';"));
    }
    
    @Test
    public void testGetPolicyForUri_DefaultSrc() {
        // Test with default-src directive
        List<String[]> grants = new ArrayList<>();
        grants.add(new String[]{"allow", "default-src", "https://example.com"});
        grants.add(new String[]{"allow", "img-src", "https://images.example.com"});
        
        CspPolicy testPolicy = new CspPolicy() {
            @Override
            public Collection<String[]> getGrants(String uri, String fact, String value) {
                return grants;
            }
        };
        
        String result = testPolicy.getPolicyForUri("/test");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("connect-src https://example.com;"));
        Assert.assertTrue(result.contains("font-src https://example.com;"));
        Assert.assertTrue(result.contains("frame-src https://example.com;"));
        Assert.assertTrue(result.contains("img-src https://example.com https://images.example.com;"));
        Assert.assertTrue(result.contains("media-src https://example.com;"));
        Assert.assertTrue(result.contains("object-src https://example.com;"));
        Assert.assertTrue(result.contains("script-src https://example.com;"));
        Assert.assertTrue(result.contains("style-src https://example.com;"));
    }
    
    @Test
    public void testGetPolicyForUri_MultipleValues() {
        // Test with multiple values for same directive
        List<String[]> grants = new ArrayList<>();
        grants.add(new String[]{"allow", "img-src", "https://example1.com"});
        grants.add(new String[]{"allow", "img-src", "https://example2.com"});
        grants.add(new String[]{"allow", "img-src", "https://example3.com"});
        
        CspPolicy testPolicy = new CspPolicy() {
            @Override
            public Collection<String[]> getGrants(String uri, String fact, String value) {
                return grants;
            }
        };
        
        String result = testPolicy.getPolicyForUri("/test");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("img-src https://example1.com https://example2.com https://example3.com"));
    }
    
    @Test
    public void testGetPolicyForUri_SortedValues() {
        // Test that values are sorted alphabetically
        List<String[]> grants = new ArrayList<>();
        grants.add(new String[]{"allow", "img-src", "z-example.com"});
        grants.add(new String[]{"allow", "img-src", "a-example.com"});
        grants.add(new String[]{"allow", "img-src", "m-example.com"});
        
        CspPolicy testPolicy = new CspPolicy() {
            @Override
            public Collection<String[]> getGrants(String uri, String fact, String value) {
                return grants;
            }
        };
        
        String result = testPolicy.getPolicyForUri("/test");
        Assert.assertNotNull(result);
        // Check that values are sorted: a-example.com, m-example.com, z-example.com
        int index1 = result.indexOf("img-src");
        int index2 = result.indexOf("a-example.com");
        int index3 = result.indexOf("m-example.com");
        int index4 = result.indexOf("z-example.com");
        Assert.assertTrue(index2 < index3 && index3 < index4);
    }
    
    @Test
    public void testLogViolation() throws Exception {
        URI uri = new URI("http://example.com/page");
        policy.logViolation(uri, "img-src", "blocked.example.com");
        // Just verify it doesn't throw an exception
        Assert.assertTrue(true);
    }
}
