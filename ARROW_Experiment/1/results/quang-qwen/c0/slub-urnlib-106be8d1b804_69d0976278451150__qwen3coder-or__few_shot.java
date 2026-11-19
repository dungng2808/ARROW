package de.slub.urn;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RQF_RFC8141_ArrowTest {

    @Test
    public void testNullInstance() {
        assertNotNull(RQF_RFC8141.NULL);
        assertEquals(EMPTY_MAP, RQF_RFC8141.NULL.resolutionParameters());
        assertEquals(EMPTY_MAP, RQF_RFC8141.NULL.queryParameters());
        assertEquals("", RQF_RFC8141.NULL.fragment());
    }

    @Test
    public void testConstructorWithValidParameters() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key2", "value2");
        String fragment = "testFragment";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);

        assertEquals(resParams, rqf.resolutionParameters());
        assertEquals(queryParams, rqf.queryParameters());
        assertEquals(fragment, rqf.fragment());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullResolutionParameters() {
        new RQF_RFC8141(null, Collections.emptyMap(), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullQueryParameters() {
        new RQF_RFC8141(Collections.emptyMap(), null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullFragment() {
        new RQF_RFC8141(Collections.emptyMap(), Collections.emptyMap(), null);
    }

    @Test
    public void testEqualsWithEqualObjects() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key2", "value2");
        String fragment = "testFragment";

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
        String fragment1 = "testFragment";

        Map<String, String> resParams2 = new HashMap<>();
        resParams2.put("key3", "value3");
        Map<String, String> queryParams2 = new HashMap<>();
        queryParams2.put("key4", "value4");
        String fragment2 = "otherFragment";

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
        String fragment = "testFragment";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);
        assertFalse(rqf.equals(null));
    }

    @Test
    public void testEqualsWithNonRQFObject() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key2", "value2");
        String fragment = "testFragment";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);
        assertFalse(rqf.equals("not an RQF object"));
    }

    @Test
    public void testHashCode() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key2", "value2");
        String fragment = "testFragment";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);
        int hashCode1 = rqf.hashCode();
        int hashCode2 = rqf.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    public void testToStringWithEmptyComponents() {
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "");
        assertEquals("", rqf.toString());
    }

    @Test
    public void testToStringWithResolutionParameters() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("key1", "value1");
        resParams.put("key2", "value2");

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, EMPTY_MAP, "");
        assertEquals("?+key1=value1&key2=value2", rqf.toString());
    }

    @Test
    public void testToStringWithQueryParameters() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key1", "value1");
        queryParams.put("key2", "value2");

        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, queryParams, "");
        assertEquals("?=key1=value1&key2=value2", rqf.toString());
    }

    @Test
    public void testToStringWithFragment() {
        RQF_RFC8141 rqf = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "fragment");
        assertEquals("#fragment", rqf.toString());
    }

    @Test
    public void testToStringWithAllComponents() {
        Map<String, String> resParams = new HashMap<>();
        resParams.put("resKey", "resValue");
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("queryKey", "queryValue");
        String fragment = "testFragment";

        RQF_RFC8141 rqf = new RQF_RFC8141(resParams, queryParams, fragment);
        assertEquals("?+resKey=resValue?=queryKey=queryValue#testFragment", rqf.toString());
    }
}
