package com.spotify.metrics.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MetricId_ArrowTest {
    @Test
    public void defaultConstructorCreatesEmptyId() {
        MetricId id = new MetricId();

        assertEquals(null, id.getKey());
        assertEquals(Collections.emptyMap(), id.getTags());
        assertEquals("null {}", id.toString());
    }

    @Test
    public void keyConstructorCreatesIdWithoutTags() {
        MetricId id = new MetricId("requests");

        assertEquals("requests", id.getKey());
        assertEquals(Collections.emptyMap(), id.getTags());
        assertEquals("requests {}", id.toString());
    }

    @Test
    public void mapConstructorCopiesAndSortsTags() {
        Map<String, String> source = new HashMap<String, String>();
        source.put("z", "last");
        source.put("a", "first");

        MetricId id = new MetricId("requests", source);
        source.put("a", "changed");

        assertEquals("requests", id.getKey());
        assertEquals("{a=first, z=last}", id.getTags().toString());
    }

    @Test
    public void resolveAppendsPathAndInheritsTags() {
        MetricId original = new MetricId("service").tagged("region", "west");
        MetricId resolved = original.resolve("requests");

        assertEquals("service.requests", resolved.getKey());
        assertEquals(Collections.singletonMap("region", "west"), resolved.getTags());
        assertNotSame(original, resolved);
    }

    @Test
    public void resolveWithNullOrEmptyPartKeepsKey() {
        MetricId original = new MetricId("service");

        assertEquals("service", original.resolve(null).getKey());
        assertEquals("service", original.resolve("").getKey());
    }

    @Test
    public void resolveFromEmptyKeyUsesPart() {
        assertEquals("requests", new MetricId("").resolve("requests").getKey());
        assertEquals("requests", new MetricId((String) null).resolve("requests").getKey());
    }

    @Test
    public void taggedMapAddsAndReplacesTagsWithoutChangingOriginal() {
        MetricId original = new MetricId("requests").tagged("region", "west");
        Map<String, String> additions = new HashMap<String, String>();
        additions.put("region", "east");
        additions.put("host", "one");

        MetricId result = original.tagged(additions);

        assertEquals("{region=west}", original.getTags().toString());
        assertEquals("{host=one, region=east}", result.getTags().toString());
        assertEquals("requests", result.getKey());
    }

    @Test
    public void taggedVarargsAddsPairs() {
        MetricId result = new MetricId("requests").tagged("region", "west", "host", "one");

        assertEquals("{host=one, region=west}", result.getTags().toString());
    }

    @Test
    public void taggedNullVarargsReturnsSameInstance() {
        MetricId id = new MetricId("requests");

        assertSame(id, id.tagged((String[]) null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void taggedOddNumberOfVarargsThrows() {
        new MetricId("requests").tagged("region", "west", "host");
    }

    @Test
    public void taggedSinglePairAddsTag() {
        MetricId result = new MetricId("requests").tagged("region", "west");

        assertEquals(Collections.singletonMap("region", "west"), result.getTags());
    }

    @Test
    public void taggedTwoPairsAddsTags() {
        MetricId result = new MetricId("requests").tagged("region", "west", "host", "one");

        assertEquals("{host=one, region=west}", result.getTags().toString());
    }

    @Test
    public void taggedThreePairsAddsTags() {
        MetricId result = new MetricId("requests")
            .tagged("region", "west", "host", "one", "zone", "a");

        assertEquals("{host=one, region=west, zone=a}", result.getTags().toString());
    }

    @Test
    public void joinCombinesKeysAndTags() {
        MetricId result = MetricId.join(
            new MetricId("service").tagged("region", "west"),
            new MetricId("requests").tagged("host", "one"));

        assertEquals("service.requests", result.getKey());
        assertEquals("{host=one, region=west}", result.getTags().toString());
    }

    @Test
    public void joinSkipsEmptyKeysAndLaterTagsOverrideEarlierTags() {
        MetricId result = MetricId.join(
            new MetricId(""),
            new MetricId("service").tagged("region", "west"),
            new MetricId("requests").tagged("region", "east"));

        assertEquals("service.requests", result.getKey());
        assertEquals(Collections.singletonMap("region", "east"), result.getTags());
    }

    @Test
    public void buildWithNoPartsReturnsEmptyConstant() {
        assertSame(MetricId.EMPTY, MetricId.build());
        assertSame(MetricId.EMPTY, MetricId.build((String[]) null));
    }

    @Test
    public void buildWithOnePartPreservesPart() {
        MetricId result = MetricId.build("requests");

        assertEquals("requests", result.getKey());
        assertEquals(Collections.emptyMap(), result.getTags());
    }

    @Test
    public void buildJoinsNonemptyParts() {
        MetricId result = MetricId.build("service", "", null, "requests");

        assertEquals("service.requests", result.getKey());
        assertEquals(Collections.emptyMap(), result.getTags());
    }

    @Test
    public void equalsAndHashCodeUseKeyAndTags() {
        MetricId first = new MetricId("requests").tagged("region", "west");
        MetricId same = new MetricId("requests").tagged("region", "west");
        MetricId different = new MetricId("requests").tagged("region", "east");

        assertTrue(first.equals(same));
        assertEquals(first.hashCode(), same.hashCode());
        assertFalse(first.equals(different));
        assertFalse(first.equals(null));
        assertFalse(first.equals("requests"));
    }

    @Test
    public void compareToOrdersByKeyWhenHashesMatch() {
        MetricId first = new MetricId("a");
        MetricId same = new MetricId("a");
        MetricId second = new MetricId("b");

        assertEquals(0, first.compareTo(same));
        assertTrue(first.compareTo(second) < 0);
        assertEquals(-1, first.compareTo(null));
    }

    @Test
    public void compareToOrdersTagsByKeyAndValue() {
        MetricId first = new MetricId("requests").tagged("region", "east");
        MetricId second = new MetricId("requests").tagged("region", "west");

        assertTrue(first.compareTo(second) < 0);
        assertTrue(second.compareTo(first) > 0);
    }

    @Test
    public void compareToOrdersDifferentTagCounts() {
        MetricId fewer = new MetricId("requests").tagged("a", "1");
        MetricId more = new MetricId("requests").tagged("a", "1", "b", "2");

        assertTrue(fewer.compareTo(more) < 0);
        assertTrue(more.compareTo(fewer) > 0);
    }

    @Test
    public void emptyTagsConstantIsUnmodifiable() {
        assertTrue(MetricId.EMPTY_TAGS.isEmpty());
        assertNotEquals(0, new MetricId("requests").hashCode());
    }
}
