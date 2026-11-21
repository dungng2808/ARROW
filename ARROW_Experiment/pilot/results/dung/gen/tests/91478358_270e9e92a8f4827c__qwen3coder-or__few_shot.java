package org.eclipse.kapua.gateway.client;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class Topic_ArrowTest {

    @Test
    public void testSplitNull() {
        Topic topic = Topic.split(null);
        assertNull(topic);
    }

    @Test
    public void testSplitEmptyString() {
        Topic topic = Topic.split("");
        assertNull(topic);
    }

    @Test
    public void testSplitOnlySlashes() {
        Topic topic = Topic.split("///");
        assertNull(topic);
    }

    @Test
    public void testSplitSingleSegment() {
        Topic topic = Topic.split("segment1");
        assertNotNull(topic);
        assertEquals(1, topic.getSegments().size());
        assertEquals("segment1", topic.getSegments().get(0));
    }

    @Test
    public void testSplitMultipleSegments() {
        Topic topic = Topic.split("segment1/segment2/segment3");
        assertNotNull(topic);
        assertEquals(3, topic.getSegments().size());
        assertEquals("segment1", topic.getSegments().get(0));
        assertEquals("segment2", topic.getSegments().get(1));
        assertEquals("segment3", topic.getSegments().get(2));
    }

    @Test
    public void testSplitLeadingTrailingSlashes() {
        Topic topic = Topic.split("/segment1/segment2/");
        assertNotNull(topic);
        assertEquals(2, topic.getSegments().size());
        assertEquals("segment1", topic.getSegments().get(0));
        assertEquals("segment2", topic.getSegments().get(1));
    }

    @Test
    public void testSplitMultipleSlashes() {
        Topic topic = Topic.split("segment1//segment2///segment3");
        assertNotNull(topic);
        assertEquals(3, topic.getSegments().size());
        assertEquals("segment1", topic.getSegments().get(0));
        assertEquals("segment2", topic.getSegments().get(1));
        assertEquals("segment3", topic.getSegments().get(2));
    }

    @Test
    public void testOfNullList() {
        Topic topic = Topic.of((List<String>) null);
        assertNull(topic);
    }

    @Test
    public void testOfEmptyList() {
        Topic topic = Topic.of(Collections.emptyList());
        assertNull(topic);
    }

    @Test
    public void testOfValidList() {
        List<String> segments = Arrays.asList("seg1", "seg2", "seg3");
        Topic topic = Topic.of(segments);
        assertNotNull(topic);
        assertEquals(3, topic.getSegments().size());
        assertEquals("seg1", topic.getSegments().get(0));
        assertEquals("seg2", topic.getSegments().get(1));
        assertEquals("seg3", topic.getSegments().get(2));
    }

    @Test
    public void testOfNullFirstSegment() {
        Topic topic = Topic.of(null, "seg2");
        assertNull(topic);
    }

    @Test
    public void testOfNullArray() {
        Topic topic = Topic.of("seg1", (String[]) null);
        assertNotNull(topic);
        assertEquals(1, topic.getSegments().size());
        assertEquals("seg1", topic.getSegments().get(0));
    }

    @Test
    public void testOfEmptyArray() {
        Topic topic = Topic.of("seg1");
        assertNotNull(topic);
        assertEquals(1, topic.getSegments().size());
        assertEquals("seg1", topic.getSegments().get(0));
    }

    @Test
    public void testOfValidArray() {
        Topic topic = Topic.of("seg1", "seg2", "seg3");
        assertNotNull(topic);
        assertEquals(3, topic.getSegments().size());
        assertEquals("seg1", topic.getSegments().get(0));
        assertEquals("seg2", topic.getSegments().get(1));
        assertEquals("seg3", topic.getSegments().get(2));
    }

    @Test
    public void testEnsureNotSpecialNull() {
        try {
            Topic.ensureNotSpecial(null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Segment must not be null or empty", e.getMessage());
        }
    }

    @Test
    public void testEnsureNotSpecialEmptyString() {
        try {
            Topic.ensureNotSpecial("");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Segment must not be null or empty", e.getMessage());
        }
    }

    @Test
    public void testEnsureNotSpecialWildcardHash() {
        try {
            Topic.ensureNotSpecial("#");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Wildcard topics are not allowed", e.getMessage());
        }
    }

    @Test
    public void testEnsureNotSpecialWildcardPlus() {
        try {
            Topic.ensureNotSpecial("+");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Wildcard topics are not allowed", e.getMessage());
        }
    }

    @Test
    public void testEnsureNotSpecialContainsSlash() {
        try {
            Topic.ensureNotSpecial("seg/ment");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Segments must not contain slashes. Use Topic.split to parse a multi-segment topic string.", e.getMessage());
        }
    }

    @Test
    public void testEnsureNotSpecialValidSegment() {
        String result = Topic.ensureNotSpecial("validSegment");
        assertEquals("validSegment", result);
    }

    @Test
    public void testToString() {
        Topic topic = Topic.of("seg1", "seg2", "seg3");
        assertEquals("seg1/seg2/seg3", topic.toString());
    }

    @Test
    public void testStream() {
        Topic topic = Topic.of("seg1", "seg2", "seg3");
        long count = topic.stream().count();
        assertEquals(3, count);
    }

    @Test
    public void testHashCode() {
        Topic topic1 = Topic.of("seg1", "seg2");
        Topic topic2 = Topic.of("seg1", "seg2");
        assertEquals(topic1.hashCode(), topic2.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        Topic topic = Topic.of("seg1", "seg2");
        assertTrue(topic.equals(topic));
    }

    @Test
    public void testEqualsNull() {
        Topic topic = Topic.of("seg1", "seg2");
        assertFalse(topic.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        Topic topic = Topic.of("seg1", "seg2");
        assertFalse(topic.equals("notATopic"));
    }

    @Test
    public void testEqualsEqualTopics() {
        Topic topic1 = Topic.of("seg1", "seg2");
        Topic topic2 = Topic.of("seg1", "seg2");
        assertTrue(topic1.equals(topic2));
    }

    @Test
    public void testEqualsDifferentSegments() {
        Topic topic1 = Topic.of("seg1", "seg2");
        Topic topic2 = Topic.of("seg1", "seg3");
        assertFalse(topic1.equals(topic2));
    }

    @Test
    public void testEqualsNullSegments() {
        Topic topic1 = Topic.of("seg1", "seg2");
        Topic topic2 = Topic.of("seg1", "seg2");
        // Both have non-null segments, so this test is just to verify normal behavior
        assertTrue(topic1.equals(topic2));
    }
}
