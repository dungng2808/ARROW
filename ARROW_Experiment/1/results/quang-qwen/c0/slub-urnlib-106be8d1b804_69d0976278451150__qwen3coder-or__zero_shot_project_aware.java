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
        queryParams.put("qkey1", "qvalue1");
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals("?=qkey1=qvalue1", rqf.toString());
        assertEquals(resParams, rqf.resolutionParameters());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals(fragment, rqf.fragment());
    }

    @Test
    public void testConstructorWithNonEmptyFragment() {
        Map<String, String> resParams = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "frag";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals("#frag", rqf.toString());
        assertEquals(resParams, rqf.resolutionParameters());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals(fragment, rqf.fragment());
    }

    @Test
    public void testConstructorWithAllComponents() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("rkey", "rvalue");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("qkey", "qvalue");
        String fragment = "frag";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals("?+rkey=rvalue?=qkey=qvalue#frag", rqf.toString());
        assertEquals(resParams, rqf.resolutionParameters());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals(fragment, rqf.fragment());
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
    public void testEqualsAndHashCode() {
        Map<String, String> resParams1 = new HashMap<>();
        resParams1.put("key1", "value1");
        Map<String, String> queryParams1 = new HashMap<>();
        queryParams1.put("qkey1", "qvalue1");
        String fragment1 = "frag1";

        Map<String, String> resParams2 = new HashMap<>();
        resParams2.put("key1", "value1");
        Map<String, String> queryParams2 = new HashMap<>();
        queryParams2.put("qkey1", "qvalue1");
        String fragment2 = "frag1";

        RQF_RFC8141 rqf1 = new RQF_RFC8141(resParams1, queryParams1, fragment1);
        RQF_RFC8141 rqf2 = new RQF_RFC8141(resParams2, queryParams2, fragment2);

        assertTrue(rqf1.equals(rqf2));
        assertEquals(rqf1.hashCode(), rqf2.hashCode());

        // Different fragment
        RQF_RFC8141 rqf3 = new RQF_RFC8141(resParams1, queryParams1, "diff");
        assertFalse(rqf1.equals(rqf3));

        // Different query params
        Map<String, String> queryParams3 = new HashMap<>();
        queryParams3.put("different", "params");
        RQF_RFC8141 rqf4 = new RQF_RFC8141(resParams1, queryParams3, fragment1);
        assertFalse(rqf1.equals(rqf4));

        // Different resolution params
        Map<String, String> resParams3 = new HashMap<>();
        resParams3.put("different", "params");
        RQF_RFC8141 rqf5 = new RQF_RFC8141(resParams3, queryParams1, fragment1);
        assertFalse(rqf1.equals(rqf5));

        // Not an instance of RQF_RFC8141
        assertFalse(rqf1.equals("not an instance"));
    }

    @Test
    public void testToStringWithMultipleResolutionParameters() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        resParams.put("key2", "value2");
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        // Order is not guaranteed, but both keys should be present
        String result = rqf.toString();
        assertTrue(result.contains("?+key1=value1"));
        assertTrue(result.contains("&"));
        assertTrue(result.contains("?+key2=value2"));
        assertTrue(result.startsWith("?+"));
    }

    @Test
    public void testToStringWithMultipleQueryParameters() {
        Map<String, String> resParams = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("qkey1", "qvalue1");
        queryParams.put("qkey2", "qvalue2");
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        // Order is not guaranteed, but both keys should be present
        String result = rqf.toString();
        assertTrue(result.contains("?=qkey1=qvalue1"));
        assertTrue(result.contains("&"));
        assertTrue(result.contains("?=qkey2=qvalue2"));
        assertTrue(result.startsWith("?="));
    }

    @Test
    public void testToStringWithSpecialCharactersInValues() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key", "value&with=and");
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "frag#with#hash";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals("?+key=value&with=and#frag#with#hash", rqf.toString());
    }

    @Test
    public void testToStringWithEmptyStringValues() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("emptyKey", "");
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals("?+emptyKey=", rqf.toString());
    }

    @Test
    public void testToStringWithNullValuesInMaps() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("nullValue", null);
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals("?+nullValue=null", rqf.toString());
    }

    @Test
    public void testResolutionParametersReturnsUnmodifiableMap() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key", "value");
        Map<String, String> queryParams = new HashMap<>();
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        Map<String, String> returnedMap = rqf.resolutionParameters();
        assertNotEquals(resParams, returnedMap);
        assertTrue(returnedMap.containsKey("key"));
        assertTrue(returnedMap.containsValue("value"));
    }

    @Test
    public void testQueryParametersReturnsUnmodifiableMap() {
        Map<String, String> resParams = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("qkey", "qvalue");
        String fragment = "";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        Map<String, String> returnedMap = rqf.queryParameters();
        assertNotEquals(queryParams, returnedMap);
        assertTrue(returnedMap.containsKey("qkey"));
        assertTrue(returnedMap.containsValue("qvalue"));
    }
}
