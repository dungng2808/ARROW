package org.eclipse.kapua.gateway.client;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class Topic_ArrowTest {

    @Test
    public void testGetSegments() {
        Topic topic = Topic.of("a", "b", "c");
        List<String> segments = topic.getSegments();
        assertEquals(3, segments.size());
        assertEquals("a", segments.get(0));
        assertEquals("b", segments.get(1));
        assertEquals("c", segments.get(2));
    }

    @Test
    public void testStream() {
        Topic topic = Topic.of("x", "y");
        long count = topic.stream().count();
        assertEquals(2L, count);
    }

    @Test
    public void testToString() {
        Topic topic = Topic.of("hello", "world");
        assertEquals("hello/world", topic.toString());
    }

    @Test
    public void testHashCode() {
        Topic t1 = Topic.of("a", "b");
        Topic t2 = Topic.of("a", "b");
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    public void testEqualsTrue() {
        Topic t1 = Topic.of("a", "b");
        Topic t2 = Topic.of("a", "b");
        assertTrue(t1.equals(t2));
    }

    @Test
    public void testEqualsFalse() {
        Topic t1 = Topic.of("a", "b");
        Topic t2 = Topic.of("a", "c");
        assertFalse(t1.equals(t2));
    }

    @Test
    public void testEqualsNull() {
        Topic t1 = Topic.of("a", "b");
        assertFalse(t1.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        Topic t1 = Topic.of("a", "b");
        assertFalse(t1.equals("not a topic"));
    }

    @Test
    public void testSplitNull() {
        assertNull(Topic.split(null));
    }

    @Test
    public void testSplitEmptyString() {
        assertNull(Topic.split(""));
    }

    @Test
    public void testSplitOnlySlashes() {
        assertNull(Topic.split("///"));
    }

    @Test
    public void testSplitNormal() {
        Topic topic = Topic.split("a/b/c");
        assertNotNull(topic);
        assertEquals(3, topic.getSegments().size());
        assertEquals("a", topic.getSegments().get(0));
        assertEquals("b", topic.getSegments().get(1));
        assertEquals("c", topic.getSegments().get(2));
    }

    @Test
    public void testSplitWithLeadingTrailingSlashes() {
        Topic topic = Topic.split("/a/b/c/");
        assertNotNull(topic);
        assertEquals(3, topic.getSegments().size());
        assertEquals("a", topic.getSegments().get(0));
        assertEquals("b", topic.getSegments().get(1));
        assertEquals("c", topic.getSegments().get(2));
    }

    @Test
    public void testSplitMultipleSlashes() {
        Topic topic = Topic.split("a///b//c");
        assertNotNull(topic);
        assertEquals(3, topic.getSegments().size());
        assertEquals("a", topic.getSegments().get(0));
        assertEquals("b", topic.getSegments().get(1));
        assertEquals("c", topic.getSegments().get(2));
    }

    @Test
    public void testOfNullList() {
        assertNull(Topic.of((List<String>) null));
    }

    @Test
    public void testOfEmptyList() {
        assertNull(Topic.of(Arrays.asList()));
    }

    @Test
    public void testOfNormal() {
        Topic topic = Topic.of(Arrays.asList("x", "y"));
        assertNotNull(topic);
        assertEquals(2, topic.getSegments().size());
        assertEquals("x", topic.getSegments().get(0));
        assertEquals("y", topic.getSegments().get(1));
    }

    @Test
    public void testOfWithSpecialSegment() {
        try {
            Topic.of(Arrays.asList("normal", "#"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testOfWithPlusSegment() {
        try {
            Topic.of(Arrays.asList("normal", "+"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testOfWithSlashInSegment() {
        try {
            Topic.of(Arrays.asList("normal", "bad/segment"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testOfSingleSegment() {
        Topic topic = Topic.of("single");
        assertNotNull(topic);
        assertEquals(1, topic.getSegments().size());
        assertEquals("single", topic.getSegments().get(0));
    }

    @Test
    public void testOfFirstNull() {
        assertNull(Topic.of((String) null));
    }

    @Test
    public void testOfFirstEmpty() {
        try {
            Topic.of("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testOfFirstSpecial() {
        try {
            Topic.of("#");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testOfFirstPlus() {
        try {
            Topic.of("+");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testOfFirstWithSlash() {
        try {
            Topic.of("bad/segment");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testOfMultipleSegments() {
        Topic topic = Topic.of("a", "b", "c");
        assertNotNull(topic);
        assertEquals(3, topic.getSegments().size());
        assertEquals("a", topic.getSegments().get(0));
        assertEquals("b", topic.getSegments().get(1));
        assertEquals("c", topic.getSegments().get(2));
    }

    @Test
    public void testEnsureNotSpecialNull() {
        try {
            Topic.ensureNotSpecial(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testEnsureNotSpecialEmpty() {
        try {
            Topic.ensureNotSpecial("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testEnsureNotSpecialHash() {
        try {
            Topic.ensureNotSpecial("#");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testEnsureNotSpecialPlus() {
        try {
            Topic.ensureNotSpecial("+");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testEnsureNotSpecialContainsSlash() {
        try {
            Topic.ensureNotSpecial("bad/segment");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testEnsureNotSpecialValid() {
        assertEquals("valid", Topic.ensureNotSpecial("valid"));
    }
}
