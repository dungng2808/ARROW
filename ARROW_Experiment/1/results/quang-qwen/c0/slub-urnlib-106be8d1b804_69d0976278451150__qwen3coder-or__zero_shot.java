package de.slub.urn;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RQF_RFC8141_ArrowTest {

    @Test
    public void testConstructorWithNullResolutionParameters() {
        try {
            new RQF_RFC8141(null, EMPTY_MAP, "");
            assertTrue(false); // Should not reach here
        } catch (IllegalArgumentException e) {
            assertEquals("Resolution parameter map cannot be null", e.getMessage());
        }
    }

    @Test
    public void testConstructorWithNullQueryParameters() {
        try {
            new RQF_RFC8141(EMPTY_MAP, null, "");
            assertTrue(false); // Should not reach here
        } catch (IllegalArgumentException e) {
            assertEquals("Query parameter map cannot be null", e.getMessage());
        }
    }

    @Test
    public void testConstructorWithNullFragment() {
        try {
            new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, null);
            assertTrue(false); // Should not reach here
        } catch (IllegalArgumentException e) {
            assertEquals("Fragment string cannot be null", e.getMessage());
        }
    }

    @Test
    public void testConstructorWithEmptyMapsAndEmptyFragment() {
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "");
        assertNotNull(rqf);
        assertEquals("", rqf.toString());
        assertTrue(rqf.resolutionParameters().isEmpty());
        assertTrue(rqf.queryParameters().isEmpty());
        assertEquals("", rqf.fragment());
    }

    @Test
    public void testConstructorWithNonEmptyResolutionParameters() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        resParams.put("key2", "value2");

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, EMPTY_MAP, "");
        assertEquals("?+key1=value1&key2=value2", rqf.toString());
        assertEquals(resParams, rqf.resolutionParameters());
        assertTrue(rqf.queryParameters().isEmpty());
        assertEquals("", rqf.fragment());
    }

    @Test
    public void testConstructorWithNonEmptyQueryParameters() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q1", "v1");
        queryParams.put("q2", "v2");

        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, queryParams, "");
        assertEquals("?=q1=v1&q2=v2", rqf.toString());
        assertTrue(rqf.resolutionParameters().isEmpty());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals("", rqf.fragment());
    }

    @Test
    public void testConstructorWithFragment() {
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "frag");
        assertEquals("#frag", rqf.toString());
        assertTrue(rqf.resolutionParameters().isEmpty());
        assertTrue(rqf.queryParameters().isEmpty());
        assertEquals("frag", rqf.fragment());
    }

    @Test
    public void testConstructorWithAllComponents() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("resKey", "resValue");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("qKey", "qValue");

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, "frag");
        assertEquals("?+resKey=resValue?=qKey=qValue#frag", rqf.toString());
        assertEquals(resParams, rqf.resolutionParameters());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals("frag", rqf.fragment());
    }

    @Test
    public void testEqualsWithSameInstance() {
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "");
        assertTrue(rqf.equals(rqf));
    }

    @Test
    public void testEqualsWithNull() {
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "");
        assertFalse(rqf.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "");
        assertFalse(rqf.equals("not a RQF_RFC8141"));
    }

    @Test
    public void testEqualsWithEqualInstances() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key", "value");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "v");

        RQF_RFC8141 rqf1 = new RQF_RFC8141(resParams, queryParams, "frag");
        RQF_RFC8141 rqf2 = new RQF_RFC8141(resParams, queryParams, "frag");

        assertTrue(rqf1.equals(rqf2));
        assertTrue(rqf2.equals(rqf1));
    }

    @Test
    public void testEqualsWithDifferentResolutionParameters() {
        Map<String, String> resParams1 = new HashMap<>();
        resParams1.put("key1", "value1");

        Map<String, String> resParams2 = new HashMap<>();
        resParams2.put("key2", "value2");

        RQF_RFC8141 rqf1 = new RQF_RFC8141(resParams1, EMPTY_MAP, "");
        RQF_RFC8141 rqf2 = new RQF_RFC8141(resParams2, EMPTY_MAP, "");

        assertFalse(rqf1.equals(rqf2));
    }

    @Test
    public void testEqualsWithDifferentQueryParameters() {
        Map<String, String> queryParams1 = new HashMap<>();
        queryParams1.put("q1", "v1");

        Map<String, String> queryParams2 = new HashMap<>();
        queryParams2.put("q2", "v2");

        RQF_RFC8141 rqf1 = new RQF_RFC8141(EMPTY_MAP, queryParams1, "");
        RQF_RFC8141 rqf2 = new RQF_RFC8141(EMPTY_MAP, queryParams2, "");

        assertFalse(rqf1.equals(rqf2));
    }

    @Test
    public void testEqualsWithDifferentFragments() {
        RQF_RFC8141 rqf1 = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "frag1");
        RQF_RFC8141 rqf2 = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "frag2");

        assertFalse(rqf1.equals(rqf2));
    }

    @Test
    public void testHashCode() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key", "value");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "v");

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, "frag");
        assertEquals(rqf.toString().hashCode(), rqf.hashCode());
    }

    @Test
    public void testNULLInstance() {
        assertNotNull(RQF_RFC8141.NULL);
        assertEquals("", RQF_RFC8141.NULL.toString());
        assertTrue(RQF_RFC8141.NULL.resolutionParameters().isEmpty());
        assertTrue(RQF_RFC8141.NULL.queryParameters().isEmpty());
        assertEquals("", RQF_RFC8141.NULL.fragment());
    }

    @Test
    public void testNULLInstanceEquality() {
        RQF_RFC8141 NULL1 = RQF_RFC8141.NULL;
        RQF_RFC8141 NULL2 = RQF_RFC8141.NULL;
        assertTrue(NULL1.equals(NULL2));
        assertEquals(NULL1.hashCode(), NULL2.hashCode());
    }

    @Test
    public void testResolutionParametersReturnsUnmodifiableMap() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key", "value");

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, EMPTY_MAP, "");
        Map<String, String> returnedMap = rqf.resolutionParameters();

        try {
            returnedMap.put("newKey", "newValue");
            assertTrue(false); // Should not reach here
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testQueryParametersReturnsUnmodifiableMap() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key", "value");

        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, queryParams, "");
        Map<String, String> returnedMap = rqf.queryParameters();

        try {
            returnedMap.put("newKey", "newValue");
            assertTrue(false); // Should not reach here
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testToStringWithOnlyResolutionParameters() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("k1", "v1");
        resParams.put("k2", "v2");

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, EMPTY_MAP, "");
        assertEquals("?+k1=v1&k2=v2", rqf.toString());
    }

    @Test
    public void testToStringWithOnlyQueryParameters() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q1", "val1");
        queryParams.put("q2", "val2");

        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, queryParams, "");
        assertEquals("?=q1=val1&q2=val2", rqf.toString());
    }

    @Test
    public void testToStringWithOnlyFragment() {
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "myFragment");
        assertEquals("#myFragment", rqf.toString());
    }

    @Test
    public void testToStringWithComplexCase() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("resKey", "resVal");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("qKey", "qVal");

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, "frag");
        assertEquals("?+resKey=resVal?=qKey=qVal#frag", rqf.toString());
    }

    @Test
    public void testToStringWithEmptyResolutionAndQueryButNonEmptyFragment() {
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "fragment");
        assertEquals("#fragment", rqf.toString());
    }

    @Test
    public void testToStringWithEmptyResolutionAndFragmentButNonEmptyQuery() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key", "value");
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, queryParams, "");
        assertEquals("?=key=value", rqf.toString());
    }

    @Test
    public void testToStringWithEmptyQueryAndFragmentButNonEmptyResolution() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key", "value");
        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, EMPTY_MAP, "");
        assertEquals("?+key=value", rqf.toString());
    }

    @Test
    public void testToStringWithEmptyStringFragment() {
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "");
        assertEquals("", rqf.toString());
    }

    @Test
    public void testFragmentGetter() {
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "testFragment");
        assertEquals("testFragment", rqf.fragment());
    }

    @Test
    public void testResolutionParametersGetter() {
        Map<String, String> expected = new HashMap<>();
        expected.put("key", "value");
        RQF_RFC8141 rqf = new RQF_RFC8141(expected, EMPTY_MAP, "");
        assertEquals(expected, rqf.resolutionParameters());
    }

    @Test
    public void testQueryParametersGetter() {
        Map<String, String> expected = new HashMap<>();
        expected.put("key", "value");
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, expected, "");
        assertEquals(expected, rqf.queryParameters());
    }
}
