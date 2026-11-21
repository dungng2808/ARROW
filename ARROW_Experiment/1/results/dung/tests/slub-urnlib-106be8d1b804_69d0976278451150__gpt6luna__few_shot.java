package de.slub.urn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

public class RQF_RFC8141_ArrowTest {

    @Test
    public void constructsAndExposesAllComponents() {
        Map<String, String> resolution = new LinkedHashMap<>();
        resolution.put("r", "one");
        Map<String, String> query = new LinkedHashMap<>();
        query.put("q", "two");

        RQF_RFC8141 rqf = new RQF_RFC8141(resolution, query, "frag");

        assertEquals(resolution, rqf.resolutionParameters());
        assertEquals(query, rqf.queryParameters());
        assertEquals("frag", rqf.fragment());
        assertEquals("?+r=one?=q=two#frag", rqf.toString());
    }

    @Test
    public void emptyComponentsHaveEmptyRepresentation() {
        RQF_RFC8141 rqf = new RQF_RFC8141(
                Collections.<String, String>emptyMap(),
                Collections.<String, String>emptyMap(),
                "");

        assertEquals("", rqf.toString());
        assertEquals(Collections.emptyMap(), rqf.resolutionParameters());
        assertEquals(Collections.emptyMap(), rqf.queryParameters());
        assertEquals("", rqf.fragment());
    }

    @Test
    public void nullObjectHasNoComponents() {
        assertEquals("", RQF_RFC8141.NULL.toString());
        assertEquals(Collections.emptyMap(), RQF_RFC8141.NULL.resolutionParameters());
        assertEquals(Collections.emptyMap(), RQF_RFC8141.NULL.queryParameters());
        assertEquals("", RQF_RFC8141.NULL.fragment());
    }

    @Test
    public void equalsAndHashCodeUseComponentsAndRepresentation() {
        Map<String, String> resolution = new LinkedHashMap<>();
        resolution.put("r", "one");
        Map<String, String> query = new LinkedHashMap<>();
        query.put("q", "two");

        RQF_RFC8141 first = new RQF_RFC8141(resolution, query, "frag");
        RQF_RFC8141 second = new RQF_RFC8141(
                new LinkedHashMap<>(resolution), new LinkedHashMap<>(query), "frag");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(RQF_RFC8141.NULL));
        assertFalse(first.equals("not an RQF"));
    }

    @Test
    public void mapsAreUnmodifiable() {
        Map<String, String> resolution = new LinkedHashMap<>();
        resolution.put("r", "one");
        Map<String, String> query = new LinkedHashMap<>();
        query.put("q", "two");
        RQF_RFC8141 rqf = new RQF_RFC8141(resolution, query, "");

        try {
            rqf.resolutionParameters().put("new", "value");
            assertTrue(false);
        } catch (UnsupportedOperationException expected) {
            assertNotNull(expected);
        }

        try {
            rqf.queryParameters().put("new", "value");
            assertTrue(false);
        } catch (UnsupportedOperationException expected) {
            assertNotNull(expected);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullQueryParameters() {
        new RQF_RFC8141(Collections.<String, String>emptyMap(), null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullFragment() {
        new RQF_RFC8141(Collections.<String, String>emptyMap(),
                Collections.<String, String>emptyMap(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullResolutionParameters() {
        new RQF_RFC8141(null, Collections.<String, String>emptyMap(), "");
    }
}
