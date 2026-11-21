package de.slub.urn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RQF_RFC8141_ArrowTest {

    @Test
    public void constructsAndReturnsAllComponents() {
        Map<String, String> resolution = new HashMap<String, String>();
        resolution.put("r", "1");
        Map<String, String> query = new HashMap<String, String>();
        query.put("q", "2");

        RQF_RFC8141 rqf = new RQF_RFC8141(resolution, query, "frag");

        assertEquals(resolution, rqf.resolutionParameters());
        assertEquals(query, rqf.queryParameters());
        assertEquals("frag", rqf.fragment());
        assertEquals("?+r=1?=q=2#frag", rqf.toString());
    }

    @Test
    public void emptyComponentsHaveEmptyRepresentation() {
        RQF_RFC8141 rqf = new RQF_RFC8141(
                new HashMap<String, String>(),
                new HashMap<String, String>(),
                "");

        assertEquals("", rqf.toString());
        assertTrue(rqf.resolutionParameters().isEmpty());
        assertTrue(rqf.queryParameters().isEmpty());
        assertEquals("", rqf.fragment());
    }

    @Test
    public void formatsResolutionQueryAndFragmentIndependently() {
        Map<String, String> resolution = new HashMap<String, String>();
        resolution.put("a", "b");
        Map<String, String> query = new HashMap<String, String>();
        query.put("x", "y");

        assertEquals("?+a=b?=x=y#z",
                new RQF_RFC8141(resolution, query, "z").toString());
        assertEquals("?+a=b",
                new RQF_RFC8141(resolution, new HashMap<String, String>(), "").toString());
        assertEquals("?=x=y",
                new RQF_RFC8141(new HashMap<String, String>(), query, "").toString());
        assertEquals("#z",
                new RQF_RFC8141(new HashMap<String, String>(), new HashMap<String, String>(), "z").toString());
    }

    @Test
    public void mapsAreUnmodifiable() {
        Map<String, String> resolution = new HashMap<String, String>();
        resolution.put("r", "1");
        Map<String, String> query = new HashMap<String, String>();
        query.put("q", "2");
        RQF_RFC8141 rqf = new RQF_RFC8141(resolution, query, "");

        try {
            rqf.resolutionParameters().put("x", "y");
            assertTrue(false);
        } catch (UnsupportedOperationException expected) {
            assertEquals("1", rqf.resolutionParameters().get("r"));
        }

        try {
            rqf.queryParameters().put("x", "y");
            assertTrue(false);
        } catch (UnsupportedOperationException expected) {
            assertEquals("2", rqf.queryParameters().get("q"));
        }
    }

    @Test
    public void equalsAndHashCodeUseRepresentationAndComponents() {
        Map<String, String> resolution1 = new HashMap<String, String>();
        resolution1.put("r", "1");
        Map<String, String> query1 = new HashMap<String, String>();
        query1.put("q", "2");

        Map<String, String> resolution2 = new HashMap<String, String>();
        resolution2.put("r", "1");
        Map<String, String> query2 = new HashMap<String, String>();
        query2.put("q", "2");

        RQF_RFC8141 first = new RQF_RFC8141(resolution1, query1, "f");
        RQF_RFC8141 second = new RQF_RFC8141(resolution2, query2, "f");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(null));
        assertFalse(first.equals("not an RQF"));
    }

    @Test
    public void unequalComponentsAreNotEqual() {
        RQF_RFC8141 first = new RQF_RFC8141(
                new HashMap<String, String>(), new HashMap<String, String>(), "");
        RQF_RFC8141 differentFragment = new RQF_RFC8141(
                new HashMap<String, String>(), new HashMap<String, String>(), "f");

        assertNotEquals(first, differentFragment);
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
        new RQF_RFC8141(new HashMap<String, String>(), new HashMap<String, String>(), null);
    }

    @Test
    public void nullInstanceHasNoComponents() {
        assertEquals("", RQF_RFC8141.NULL.toString());
        assertTrue(RQF_RFC8141.NULL.resolutionParameters().isEmpty());
        assertTrue(RQF_RFC8141.NULL.queryParameters().isEmpty());
        assertEquals("", RQF_RFC8141.NULL.fragment());
    }
}
