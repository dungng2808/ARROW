package de.slub.urn;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RQF_RFC8141_ArrowTest {

    @Test
    public void testNullInstance() {
        assertSame(RQF_RFC8141.NULL, RQF_RFC8141.NULL);
        assertEquals("", RQF_RFC8141.NULL.toString());
        assertTrue(RQF_RFC8141.NULL.resolutionParameters().isEmpty());
        assertTrue(RQF_RFC8141.NULL.queryParameters().isEmpty());
        assertEquals("", RQF_RFC8141.NULL.fragment());
    }

    @Test
    public void testConstructorWithValidParameters() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key2", "value2");
        String fragment = "fragment";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals(resParams, rqf.resolutionParameters());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals(fragment, rqf.fragment());
        assertFalse(rqf.toString().isEmpty());
    }

    @Test
    public void testConstructorWithEmptyMapsAndString() {
        Map<String, String> resParams = Collections.emptyMap();
        Map<String, String> queryParams = Collections.emptyMap();
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals(resParams, rqf.resolutionParameters());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals(fragment, rqf.fragment());
        assertEquals("", rqf.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullResolutionParameters() {
        new RQF_RFC8141(null, EMPTY_MAP, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullQueryParameters() {
        new RQF_RFC8141(EMPTY_MAP, null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullFragment() {
        new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, null);
    }

    @Test
    public void testEqualsWithEqualObjects() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key2", "value2");
        String fragment = "fragment";

        RQF_RFC8141 rqf1 = new RQF_RFC8141(resParams, queryParams, fragment);
        RQF_RFC8141 rqf2 = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals(rqf1, rqf2);
    }

    @Test
    public void testEqualsWithDifferentObjects() {
        Map<String, String> resParams1 = new HashMap<>();
        resParams1.put("key1", "value1");
        Map<String, String> queryParams1 = new HashMap<>();
        queryParams1.put("key2", "value2");
        String fragment1 = "fragment";

        Map<String, String> resParams2 = new HashMap<>();
        resParams2.put("key3", "value3");
        Map<String, String> queryParams2 = new HashMap<>();
        queryParams2.put("key4", "value4");
        String fragment2 = "fragment2";

        RQF_RFC8141 rqf1 = new RQF_RFC8141(resParams1, queryParams1, fragment1);
        RQF_RFC8141 rqf2 = new RQF_RFC8141(resParams2, queryParams2, fragment2);

        assertNotEquals(rqf1, rqf2);
    }

    @Test
    public void testEqualsWithNullObject() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key2", "value2");
        String fragment = "fragment";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);
        assertNotEquals(rqf, null);
    }

    @Test
    public void testEqualsWithNonRQFObject() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key2", "value2");
        String fragment = "fragment";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);
        assertNotEquals(rqf, "not an RQF object");
    }

    @Test
    public void testHashCode() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key2", "value2");
        String fragment = "fragment";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);
        int hashCode1 = rqf.hashCode();
        int hashCode2 = rqf.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    public void testToStringWithAllComponents() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        resParams.put("key2", "value2");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key3", "value3");
        String fragment = "fragment";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);
        String result = rqf.toString();

        assertTrue(result.contains("?+"));
        assertTrue(result.contains("key1=value1"));
        assertTrue(result.contains("key2=value2"));
        assertTrue(result.contains("?="));
        assertTrue(result.contains("key3=value3"));
        assertTrue(result.contains("#fragment"));
    }

    @Test
    public void testToStringWithOnlyResolutionParameters() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = Collections.emptyMap();
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);
        String result = rqf.toString();

        assertTrue(result.startsWith("?+"));
        assertTrue(result.contains("key1=value1"));
        assertFalse(result.contains("?="));
        assertFalse(result.contains("#"));
    }

    @Test
    public void testToStringWithOnlyQueryParameters() {
        Map<String, String> resParams = Collections.emptyMap();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key1", "value1");
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);
        String result = rqf.toString();

        assertTrue(result.startsWith("?="));
        assertTrue(result.contains("key1=value1"));
        assertFalse(result.contains("?+"));
        assertFalse(result.contains("#"));
    }

    @Test
    public void testToStringWithOnlyFragment() {
        Map<String, String> resParams = Collections.emptyMap();
        Map<String, String> queryParams = Collections.emptyMap();
        String fragment = "fragment";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);
        String result = rqf.toString();

        assertTrue(result.startsWith("#"));
        assertTrue(result.endsWith("fragment"));
        assertFalse(result.contains("?+"));
        assertFalse(result.contains("?="));
    }

    @Test
    public void testResolutionParametersReturnsUnmodifiableMap() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, EMPTY_MAP, "");

        Map<String, String> returnedMap = rqf.resolutionParameters();
        // Should be the same instance since it's unmodifiable
        assertSame(resParams, returnedMap);
    }

    @Test
    public void testQueryParametersReturnsUnmodifiableMap() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key1", "value1");
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, queryParams, "");

        Map<String, String> returnedMap = rqf.queryParameters();
        // Should be the same instance since it's unmodifiable
        assertSame(queryParams, returnedMap);
    }
}
