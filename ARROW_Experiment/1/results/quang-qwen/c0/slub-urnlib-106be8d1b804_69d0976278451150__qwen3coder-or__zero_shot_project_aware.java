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
    public void testNullInstance() {
        assertNotNull(RQF_RFC8141.NULL);
        assertEquals("", RQF_RFC8141.NULL.toString());
        assertEquals(EMPTY_MAP, RQF_RFC8141.NULL.resolutionParameters());
        assertEquals(EMPTY_MAP, RQF_RFC8141.NULL.queryParameters());
        assertEquals("", RQF_RFC8141.NULL.fragment());
    }

    @Test
    public void testConstructorWithEmptyMapsAndEmptyFragment() {
        Map<String, String> resParams = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals("", rqf.toString());
        assertEquals(resParams, rqf.resolutionParameters());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals(fragment, rqf.fragment());
    }

    @Test
    public void testConstructorWithNonEmptyResolutionParameters() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals("?+key1=value1", rqf.toString());
        assertEquals(resParams, rqf.resolutionParameters());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals(fragment, rqf.fragment());
    }

    @Test
    public void testConstructorWithNonEmptyQueryParameters() {
        Map<String, String> resParams = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "search");
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals("?=q=search", rqf.toString());
        assertEquals(resParams, rqf.resolutionParameters());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals(fragment, rqf.fragment());
    }

    @Test
    public void testConstructorWithNonEmptyFragment() {
        Map<String, String> resParams = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "section1";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals("#section1", rqf.toString());
        assertEquals(resParams, rqf.resolutionParameters());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals(fragment, rqf.fragment());
    }

    @Test
    public void testConstructorWithAllComponents() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "search");
        String fragment = "section1";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals("?+key1=value1?=q=search#section1", rqf.toString());
        assertEquals(resParams, rqf.resolutionParameters());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals(fragment, rqf.fragment());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullResolutionParameters() {
        new RQF_RFC8141(null, new HashMap<>(), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullQueryParameters() {
        new RQF_RFC8141(new HashMap<>(), null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullFragment() {
        new RQF_RFC8141(new HashMap<>(), new HashMap<>(), null);
    }

    @Test
    public void testEqualsWithSameInstance() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "search");
        String fragment = "section1";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertTrue(rqf.equals(rqf));
    }

    @Test
    public void testEqualsWithEqualInstances() {
        Map<String, String> resParams1 = new HashMap<>();
        resParams1.put("key1", "value1");
        Map<String, String> queryParams1 = new HashMap<>();
        queryParams1.put("q", "search");
        String fragment1 = "section1";

        Map<String, String> resParams2 = new HashMap<>();
        resParams2.put("key1", "value1");
        Map<String, String> queryParams2 = new HashMap<>();
        queryParams2.put("q", "search");
        String fragment2 = "section1";

        RQF_RFC8141 rqf1 = new RQF_RFC8141(resParams1, queryParams1, fragment1);
        RQF_RFC8141 rqf2 = new RQF_RFC8141(resParams2, queryParams2, fragment2);

        assertTrue(rqf1.equals(rqf2));
        assertTrue(rqf2.equals(rqf1));
    }

    @Test
    public void testEqualsWithDifferentResolutionParameters() {
        Map<String, String> resParams1 = new HashMap<>();
        resParams1.put("key1", "value1");
        Map<String, String> queryParams1 = new HashMap<>();
        String fragment1 = "";

        Map<String, String> resParams2 = new HashMap<>();
        resParams2.put("key2", "value2");
        Map<String, String> queryParams2 = new HashMap<>();
        String fragment2 = "";

        RQF_RFC8141 rqf1 = new RQF_RFC8141(resParams1, queryParams1, fragment1);
        RQF_RFC8141 rqf2 = new RQF_RFC8141(resParams2, queryParams2, fragment2);

        assertFalse(rqf1.equals(rqf2));
    }

    @Test
    public void testEqualsWithDifferentQueryParameters() {
        Map<String, String> resParams1 = new HashMap<>();
        String fragment1 = "";

        Map<String, String> queryParams1 = new HashMap<>();
        queryParams1.put("q", "search");

        Map<String, String> resParams2 = new HashMap<>();
        String fragment2 = "";

        Map<String, String> queryParams2 = new HashMap<>();
        queryParams2.put("q", "different");

        RQF_RFC8141 rqf1 = new RQF_RFC8141(resParams1, queryParams1, fragment1);
        RQF_RFC8141 rqf2 = new RQF_RFC8141(resParams2, queryParams2, fragment2);

        assertFalse(rqf1.equals(rqf2));
    }

    @Test
    public void testEqualsWithDifferentFragments() {
        Map<String, String> resParams1 = new HashMap<>();
        String fragment1 = "section1";

        Map<String, String> queryParams1 = new HashMap<>();

        Map<String, String> resParams2 = new HashMap<>();
        String fragment2 = "section2";

        Map<String, String> queryParams2 = new HashMap<>();

        RQF_RFC8141 rqf1 = new RQF_RFC8141(resParams1, queryParams1, fragment1);
        RQF_RFC8141 rqf2 = new RQF_RFC8141(resParams2, queryParams2, fragment2);

        assertFalse(rqf1.equals(rqf2));
    }

    @Test
    public void testEqualsWithNullObject() {
        Map<String, String> resParams = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertFalse(rqf.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        Map<String, String> resParams = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertFalse(rqf.equals("not an RQF_RFC8141"));
    }

    @Test
    public void testHashCode() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "search");
        String fragment = "section1";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals(rqf.toString().hashCode(), rqf.hashCode());
    }

    @Test
    public void testHashCodeConsistency() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "search");
        String fragment = "section1";

        RQF_RFC8141 rqf1 = new RQF_RFC8141(resParams, queryParams, fragment);
        RQF_RFC8141 rqf2 = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals(rqf1.hashCode(), rqf2.hashCode());
    }

    @Test
    public void testToStringWithMultipleResolutionParameters() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        resParams.put("key2", "value2");
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        // Order is not guaranteed, so we just check that both parameters are present
        String result = rqf.toString();
        assertTrue(result.contains("?+key1=value1"));
        assertTrue(result.contains("&"));
        assertTrue(result.contains("key2=value2"));
    }

    @Test
    public void testToStringWithMultipleQueryParameters() {
        Map<String, String> resParams = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "search");
        queryParams.put("lang", "de");
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        // Order is not guaranteed, so we just check that both parameters are present
        String result = rqf.toString();
        assertTrue(result.contains("?=q=search"));
        assertTrue(result.contains("&"));
        assertTrue(result.contains("lang=de"));
    }

    @Test
    public void testToStringWithComplexStringRepresentation() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("type", "book");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("author", "Smith");
        queryParams.put("title", "Java Programming");
        String fragment = "chapter3";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        String result = rqf.toString();
        assertTrue(result.contains("?+type=book"));
        assertTrue(result.contains("?=author=Smith"));
        assertTrue(result.contains("&"));
        assertTrue(result.contains("title=Java Programming"));
        assertTrue(result.contains("#chapter3"));
    }

    @Test
    public void testResolutionParametersReturnsUnmodifiableMap() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        Map<String, String> returnedMap = rqf.resolutionParameters();
        // Should not be able to modify the returned map
        try {
            returnedMap.put("newKey", "newValue");
            // If we reach here, the map was modifiable, which is wrong
            fail("Returned map should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // Expected behavior
        }
    }

    @Test
    public void testQueryParametersReturnsUnmodifiableMap() {
        Map<String, String> resParams = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "search");
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        Map<String, String> returnedMap = rqf.queryParameters();
        // Should not be able to modify the returned map
        try {
            returnedMap.put("newKey", "newValue");
            // If we reach here, the map was modifiable, which is wrong
            fail("Returned map should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // Expected behavior
        }
    }

    private void fail(String message) {
        throw new AssertionError(message);
    }
}
