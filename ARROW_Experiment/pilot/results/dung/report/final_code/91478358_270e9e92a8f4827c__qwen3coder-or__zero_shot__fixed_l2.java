package org.eclipse.kapua.gateway.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class Topic_ArrowTest {

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
    public void testSplitSingleSegment() {
        Topic topic = Topic.split("test");
        assertNotNull(topic);
        assertEquals(1, topic.getSegments().size());
        assertEquals("test", topic.getSegments().get(0));
    }

    @Test
    public void testSplitMultipleSegments() {
        Topic topic = Topic.split("a/b/c");
        assertNotNull(topic);
        assertEquals(3, topic.getSegments().size());
        assertEquals("a", topic.getSegments().get(0));
        assertEquals("b", topic.getSegments().get(1));
        assertEquals("c", topic.getSegments().get(2));
    }

    @Test
    public void testSplitLeadingTrailingSlashes() {
        Topic topic = Topic.split("/a/b/");
        assertNotNull(topic);
        assertEquals(2, topic.getSegments().size());
        assertEquals("a", topic.getSegments().get(0));
        assertEquals("b", topic.getSegments().get(1));
    }

    @Test
    public void testSplitMultipleSlashes() {
        Topic topic = Topic.split("a//b///c");
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
    public void testOfValidList() {
        List<String> segments = Arrays.asList("a", "b", "c");
        Topic topic = Topic.of(segments);
        assertNotNull(topic);
        assertEquals(3, topic.getSegments().size());
        assertEquals("a", topic.getSegments().get(0));
        assertEquals("b", topic.getSegments().get(1));
        assertEquals("c", topic.getSegments().get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOfListWithNullSegment() {
        List<String> segments = Arrays.asList("a", null, "c");
        Topic.of(segments);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOfListWithEmptySegment() {
        List<String> segments = Arrays.asList("a", "", "c");
        Topic.of(segments);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOfListWithWildcardSegment() {
        List<String> segments = Arrays.asList("a", "#", "c");
        Topic.of(segments);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOfListWithPlusSegment() {
        List<String> segments = Arrays.asList("a", "+", "c");
        Topic.of(segments);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOfListWithSlashSegment() {
        List<String> segments = Arrays.asList("a", "b/c", "d");
        Topic.of(segments);
    }

    @Test
    public void testOfNullFirst() {
        assertNull(Topic.of(null));
    }

    @Test
    public void testOfSingleSegment() {
        Topic topic = Topic.of("test");
        assertNotNull(topic);
        assertEquals(1, topic.getSegments().size());
        assertEquals("test", topic.getSegments().get(0));
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
    public void testOfNullVarargs() {
        Topic topic = Topic.of("a", (String[]) null);
        assertNotNull(topic);
        assertEquals(1, topic.getSegments().size());
        assertEquals("a", topic.getSegments().get(0));
    }

    @Test
    public void testOfEmptyVarargs() {
        Topic topic = Topic.of("a");
        assertNotNull(topic);
        assertEquals(1, topic.getSegments().size());
        assertEquals("a", topic.getSegments().get(0));
    }

    @Test
    public void testOfNullFirstInVarargs() {
        assertNull(Topic.of(null, "b", "c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOfNullSegmentInVarargs() {
        Topic.of("a", null, "c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOfEmptySegmentInVarargs() {
        Topic.of("a", "", "c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOfWildcardSegmentInVarargs() {
        Topic.of("a", "#", "c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOfPlusSegmentInVarargs() {
        Topic.of("a", "+", "c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOfSlashSegmentInVarargs() {
        Topic.of("a", "b/c", "d");
    }

    @Test
    public void testEnsureNotSpecialNull() {
        try {
            Topic.ensureNotSpecial(null);
            assertTrue(false); // Should not reach here
        } catch (IllegalArgumentException e) {
            assertEquals("Segment must not be null or empty", e.getMessage());
        }
    }

    @Test
    public void testEnsureNotSpecialEmpty() {
        try {
            Topic.ensureNotSpecial("");
            assertTrue(false); // Should not reach here
        } catch (IllegalArgumentException e) {
            assertEquals("Segment must not be null or empty", e.getMessage());
        }
    }

    @Test
    public void testEnsureNotSpecialWildcardHash() {
        try {
            Topic.ensureNotSpecial("#");
            assertTrue(false); // Should not reach here
        } catch (IllegalArgumentException e) {
            assertEquals("Wildcard topics are not allowed", e.getMessage());
        }
    }

    @Test
    public void testEnsureNotSpecialWildcardPlus() {
        try {
            Topic.ensureNotSpecial("+");
            assertTrue(false); // Should not reach here
        } catch (IllegalArgumentException e) {
            assertEquals("Wildcard topics are not allowed", e.getMessage());
        }
    }

    @Test
    public void testEnsureNotSpecialContainsSlash() {
        try {
            Topic.ensureNotSpecial("a/b");
            assertTrue(false); // Should not reach here
        } catch (IllegalArgumentException e) {
            assertEquals("Segments must not contain slashes. Use Topic.split to parse a multi-segment topic string.", e.getMessage());
        }
    }

    @Test
    public void testEnsureNotSpecialValidSegment() {
        assertEquals("valid", Topic.ensureNotSpecial("valid"));
    }

    @Test
    public void testToString() {
        Topic topic = Topic.of("a", "b", "c");
        assertEquals("a/b/c", topic.toString());
    }

    @Test
    public void testStream() {
        Topic topic = Topic.of("a", "b", "c");
        assertEquals(3, topic.stream().count());
    }

    @Test
    public void testHashCodeEqualTopics() {
        Topic t1 = Topic.of("a", "b");
        Topic t2 = Topic.of("a", "b");
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    public void testHashCodeDifferentTopics() {
        Topic t1 = Topic.of("a", "b");
        Topic t2 = Topic.of("a", "c");
        assertFalse(t1.hashCode() == t2.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        Topic t1 = Topic.of("a", "b");
        assertTrue(t1.equals(t1));
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
    public void testEqualsEqualTopics() {
        Topic t1 = Topic.of("a", "b");
        Topic t2 = Topic.of("a", "b");
        assertTrue(t1.equals(t2));
        assertTrue(t2.equals(t1));
    }

    @Test
    public void testEqualsDifferentTopics() {
        Topic t1 = Topic.of("a", "b");
        Topic t2 = Topic.of("a", "c");
        assertFalse(t1.equals(t2));
        assertFalse(t2.equals(t1));
    }

    @Test
    public void testEqualsNullSegments() {
        Topic t1 = Topic.of("a", "b");
        Topic t2 = Topic.of("a", "b");
        // Both have non-null segments, so they should be equal
        assertTrue(t1.equals(t2));
    }
}
