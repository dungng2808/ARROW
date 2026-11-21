package de.slub.urn;

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
    public void formatsAllComponents() {
        Map<String, String> resolution = new HashMap<String, String>();
        resolution.put("r", "1");
        Map<String, String> query = new HashMap<String, String>();
        query.put("q", "2");

        RQF_RFC8141 rqf = new RQF_RFC8141(resolution, query, "frag");

        assertEquals("?+r=1?=q=2#frag", rqf.toString());
    }

    @Test
    public void formatsEmptyComponentsAsEmptyString() {
        RQF_RFC8141 rqf = new RQF_RFC8141(
                new HashMap<String, String>(),
                new HashMap<String, String>(),
                "");

        assertEquals("", rqf.toString());
    }

    @Test
    public void exposesResolutionParameters() {
        Map<String, String> resolution = new HashMap<String, String>();
        resolution.put("key", "value");
        RQF_RFC8141 rqf = new RQF_RFC8141(
                resolution, new HashMap<String, String>(), "");

        assertEquals(resolution, rqf.resolutionParameters());
    }

    @Test
    public void exposesQueryParameters() {
        Map<String, String> query = new HashMap<String, String>();
        query.put("key", "value");
        RQF_RFC8141 rqf = new RQF_RFC8141(
                new HashMap<String, String>(), query, "");

        assertEquals(query, rqf.queryParameters());
    }

    @Test
    public void exposesFragment() {
        RQF_RFC8141 rqf = new RQF_RFC8141(
                new HashMap<String, String>(),
                new HashMap<String, String>(),
                "fragment");

        assertEquals("fragment", rqf.fragment());
    }

    @Test
    public void returnedParameterMapsAreUnmodifiable() {
        Map<String, String> resolution = new HashMap<String, String>();
        resolution.put("key", "value");
        RQF_RFC8141 rqf = new RQF_RFC8141(
                resolution, new HashMap<String, String>(), "");

        try {
            rqf.resolutionParameters().put("other", "value");
        } catch (UnsupportedOperationException expected) {
            assertEquals("value", rqf.resolutionParameters().get("key"));
            return;
        }
        throw new AssertionError("Expected an unmodifiable map");
    }

    @Test
    public void equalsComparesComponents() {
        Map<String, String> resolution = new HashMap<String, String>();
        resolution.put("r", "1");
        Map<String, String> query = new HashMap<String, String>();
        query.put("q", "2");

        RQF_RFC8141 first = new RQF_RFC8141(resolution, query, "frag");
        RQF_RFC8141 second = new RQF_RFC8141(
                new HashMap<String, String>(resolution),
                new HashMap<String, String>(query),
                "frag");

        assertTrue(first.equals(second));
    }

    @Test
    public void equalsRejectsDifferentComponentsAndOtherTypes() {
        RQF_RFC8141 rqf = new RQF_RFC8141(
                new HashMap<String, String>(),
                new HashMap<String, String>(),
                "");

        assertFalse(rqf.equals(new RQF_RFC8141(
                new HashMap<String, String>(),
                new HashMap<String, String>(),
                "different")));
        assertFalse(rqf.equals("not an RQF"));
    }

    @Test
    public void equalInstancesHaveEqualHashCodes() {
        RQF_RFC8141 first = new RQF_RFC8141(
                new HashMap<String, String>(),
                new HashMap<String, String>(),
                "frag");
        RQF_RFC8141 second = new RQF_RFC8141(
                new HashMap<String, String>(),
                new HashMap<String, String>(),
                "frag");

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void nullInstanceHasNoComponents() {
        assertNotNull(RQF_RFC8141.NULL);
        assertEquals("", RQF_RFC8141.NULL.toString());
        assertTrue(RQF_RFC8141.NULL.resolutionParameters().isEmpty());
        assertTrue(RQF_RFC8141.NULL.queryParameters().isEmpty());
        assertEquals("", RQF_RFC8141.NULL.fragment());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullResolutionParameters() {
        new RQF_RFC8141(null, new HashMap<String, String>(), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullQueryParameters() {
        new RQF_RFC8141(new HashMap<String, String>(), null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullFragment() {
        new RQF_RFC8141(
                new HashMap<String, String>(),
                new HashMap<String, String>(),
                null);
    }
}
