package org.eclipse.kapua.gateway.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class Topic_ArrowTest {

    @Test
    public void splitParsesAndNormalizesPath() {
        Topic topic = Topic.split("///alpha//beta///");
        assertEquals(Arrays.asList("alpha", "beta"), topic.getSegments());
        assertEquals("alpha/beta", topic.toString());
    }

    @Test
    public void splitReturnsNullForNullOrEmptyPath() {
        assertNull(Topic.split(null));
        assertNull(Topic.split(""));
        assertNull(Topic.split("///"));
    }

    @Test
    public void ofListCopiesSegmentsAndPreservesOrder() {
        List<String> segments = new ArrayList<>(Arrays.asList("alpha", "beta"));
        Topic topic = Topic.of(segments);
        segments.set(0, "changed");

        assertEquals(Arrays.asList("alpha", "beta"), topic.getSegments());
        assertEquals(Arrays.asList("alpha", "beta"), topic.stream().collect(Collectors.toList()));
    }

    @Test
    public void ofListReturnsNullForNullOrEmptyList() {
        assertNull(Topic.of((List<String>) null));
        assertNull(Topic.of(Collections.<String>emptyList()));
    }

    @Test
    public void ofStringsCreatesTopic() {
        Topic topic = Topic.of("alpha", "beta", "gamma");
        assertEquals(Arrays.asList("alpha", "beta", "gamma"), topic.getSegments());
        assertEquals("alpha/beta/gamma", topic.toString());
    }

    @Test
    public void ofStringsHandlesNullFirstAndMissingVarargs() {
        assertNull(Topic.of(null, "beta"));
        assertEquals(Collections.singletonList("alpha"), Topic.of("alpha", (String[]) null).getSegments());
        assertEquals(Collections.singletonList("alpha"), Topic.of("alpha").getSegments());
    }

    @Test
    public void ensureNotSpecialReturnsValidSegment() {
        assertEquals("alpha", Topic.ensureNotSpecial("alpha"));
    }

    @Test
    public void ensureNotSpecialRejectsInvalidSegments() {
        String[] invalid = {null, "", "#", "+", "alpha/beta"};
        for (String segment : invalid) {
            try {
                Topic.ensureNotSpecial(segment);
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().length() > 0);
            }
        }
    }

    @Test
    public void ofRejectsSpecialSegments() {
        String[] invalid = {null, "", "#", "+", "alpha/beta"};
        for (String segment : invalid) {
            try {
                Topic.of(Arrays.asList(segment));
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().length() > 0);
            }
        }
    }

    @Test
    public void segmentsAreUnmodifiable() {
        Topic topic = Topic.of("alpha");
        try {
            topic.getSegments().add("beta");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            assertEquals(Collections.singletonList("alpha"), topic.getSegments());
        }
    }

    @Test
    public void equalityAndHashCodeUseSegments() {
        Topic first = Topic.of("alpha", "beta");
        Topic same = Topic.split("/alpha/beta/");
        Topic different = Topic.of("alpha", "gamma");

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, different);
        assertFalse(first.equals(null));
        assertTrue(first.equals(first));
    }

    @Test
    public void splitAndOfProduceEqualTopics() {
        Topic split = Topic.split("alpha/beta");
        Topic created = Topic.of(Arrays.asList("alpha", "beta"));
        assertSame(split.getClass(), created.getClass());
        assertEquals(split, created);
    }
}
