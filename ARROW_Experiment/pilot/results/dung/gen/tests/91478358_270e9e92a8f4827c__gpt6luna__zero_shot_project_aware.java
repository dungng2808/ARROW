package org.eclipse.kapua.gateway.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Topic_ArrowTest {

    @Test
    public void splitParsesAndTrimsPath() {
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
    public void ofListCopiesSegmentsAndReturnsUnmodifiableSegments() {
        List<String> input = new ArrayList<>(Arrays.asList("alpha", "beta"));
        Topic topic = Topic.of(input);
        input.set(0, "changed");

        assertEquals(Arrays.asList("alpha", "beta"), topic.getSegments());
        try {
            topic.getSegments().add("gamma");
        } catch (UnsupportedOperationException expected) {
            assertEquals(Arrays.asList("alpha", "beta"), topic.getSegments());
            return;
        }
        throw new AssertionError("segments should be unmodifiable");
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
    public void ofSingleStringCreatesTopic() {
        Topic topic = Topic.of("alpha");
        assertEquals(Collections.singletonList("alpha"), topic.getSegments());
        assertEquals("alpha", topic.toString());
    }

    @Test
    public void ofStringsReturnsNullForNullFirstSegment() {
        assertNull(Topic.of(null, "beta"));
    }

    @Test
    public void streamReturnsSegmentsInOrder() {
        Topic topic = Topic.of("alpha", "beta");
        assertEquals(Arrays.asList("alpha", "beta"), topic.stream().collect(Collectors.toList()));
    }

    @Test
    public void ensureNotSpecialReturnsValidSegment() {
        assertEquals("alpha", Topic.ensureNotSpecial("alpha"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNotSpecialRejectsNull() {
        Topic.ensureNotSpecial(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNotSpecialRejectsEmpty() {
        Topic.ensureNotSpecial("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNotSpecialRejectsHashWildcard() {
        Topic.ensureNotSpecial("#");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNotSpecialRejectsPlusWildcard() {
        Topic.ensureNotSpecial("+");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNotSpecialRejectsSlash() {
        Topic.ensureNotSpecial("alpha/beta");
    }

    @Test
    public void equalsAndHashCodeUseSegments() {
        Topic first = Topic.of("alpha", "beta");
        Topic same = Topic.of(Arrays.asList("alpha", "beta"));
        Topic different = Topic.of("alpha", "gamma");

        assertTrue(first.equals(same));
        assertEquals(first.hashCode(), same.hashCode());
        assertFalse(first.equals(different));
        assertFalse(first.equals(null));
        assertFalse(first.equals("alpha/beta"));
        assertSame(first, first);
        assertNotEquals(first.hashCode(), different.hashCode());
    }
}
