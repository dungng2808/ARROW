package com.spotify.metrics.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MetricId_ArrowTest {

    @Test
    public void defaultConstructorCreatesEmptyMetricId() {
        MetricId metricId = new MetricId();

        assertEquals(null, metricId.getKey());
        assertEquals(Collections.emptyMap(), metricId.getTags());
        assertEquals("null {}", metricId.toString());
    }

    @Test
    public void keyConstructorCreatesMetricWithoutTags() {
        MetricId metricId = new MetricId("requests");

        assertEquals("requests", metricId.getKey());
        assertEquals(Collections.emptyMap(), metricId.getTags());
    }

    @Test
    public void mapConstructorSortsTagsAndKeepsThemUnmodifiable() {
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("z", "last");
        tags.put("a", "first");

        MetricId metricId = new MetricId("requests", tags);

        assertEquals("requests", metricId.getKey());
        assertEquals("{a=first, z=last}", metricId.getTags().toString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void tagsCannotBeModified() {
        MetricId metricId = new MetricId("requests");
        metricId.getTags().put("region", "west");
    }

    @Test
    public void resolveAppendsPathAndInheritsTags() {
        MetricId original = new MetricId("service").tagged("region", "west");
        MetricId resolved = original.resolve("requests");

        assertEquals("service.requests", resolved.getKey());
        assertEquals(Collections.singletonMap("region", "west"), resolved.getTags());
    }

    @Test
    public void resolveWithEmptyPartKeepsKey() {
        MetricId metricId = new MetricId("service").resolve("");

        assertEquals("service", metricId.getKey());
    }

    @Test
    public void resolveWithNullPartKeepsKey() {
        MetricId metricId = new MetricId("service").resolve(null);

        assertEquals("service", metricId.getKey());
    }

    @Test
    public void resolveFromEmptyKeyUsesPart() {
        MetricId metricId = new MetricId("").resolve("requests");

        assertEquals("requests", metricId.getKey());
    }

    @Test
    public void taggedMapAddsAndOverridesTags() {
        Map<String, String> additions = new HashMap<String, String>();
        additions.put("region", "east");
        additions.put("host", "one");

        MetricId metricId = new MetricId("service").tagged("region", "west").tagged(additions);

        assertEquals("{host=one, region=east}", metricId.getTags().toString());
    }

    @Test
    public void taggedVarargsAddsPairs() {
        MetricId metricId = new MetricId("service").tagged("region", "west", "host", "one");

        assertEquals("{host=one, region=west}", metricId.getTags().toString());
    }

    @Test
    public void taggedNullVarargsReturnsSameInstance() {
        MetricId metricId = new MetricId("service");

        assertSame(metricId, metricId.tagged((String[]) null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void taggedOddNumberOfVarargsThrows() {
        new MetricId("service").tagged("region");
    }

    @Test
    public void taggedSinglePairAddsTag() {
        MetricId metricId = new MetricId("service").tagged("region", "west");

        assertEquals(Collections.singletonMap("region", "west"), metricId.getTags());
    }

    @Test
    public void taggedTwoPairsAddsTags() {
        MetricId metricId = new MetricId("service").tagged("region", "west", "host", "one");

        assertEquals("{host=one, region=west}", metricId.getTags().toString());
    }

    @Test
    public void taggedThreePairsAddsTags() {
        MetricId metricId = new MetricId("service")
            .tagged("region", "west", "host", "one", "zone", "a");

        assertEquals("{host=one, region=west, zone=a}", metricId.getTags().toString());
    }

    @Test
    public void joinCombinesKeysAndTags() {
        MetricId first = new MetricId("service").tagged("region", "west");
        MetricId second = new MetricId("requests").tagged("host", "one");

        MetricId joined = MetricId.join(first, second);

        assertEquals("service.requests", joined.getKey());
        assertEquals("{host=one, region=west}", joined.getTags().toString());
    }

    @Test
    public void joinSkipsEmptyKeysAndLaterTagsOverrideEarlierTags() {
        MetricId first = new MetricId("").tagged("region", "west");
        MetricId second = new MetricId("requests").tagged("region", "east");

        MetricId joined = MetricId.join(first, second);

        assertEquals("requests", joined.getKey());
        assertEquals(Collections.singletonMap("region", "east"), joined.getTags());
    }

    @Test
    public void buildWithNoPartsReturnsEmptyConstant() {
        assertSame(MetricId.EMPTY, MetricId.build());
    }

    @Test
    public void buildWithNullPartsReturnsEmptyConstant() {
        assertSame(MetricId.EMPTY, MetricId.build((String[]) null));
    }

    @Test
    public void buildWithOnePartPreservesPart() {
        MetricId metricId = MetricId.build("requests");

        assertEquals("requests", metricId.getKey());
        assertEquals(Collections.emptyMap(), metricId.getTags());
    }

    @Test
    public void buildJoinsNonemptyParts() {
        MetricId metricId = MetricId.build("service", "", "requests", null);

        assertEquals("service.requests", metricId.getKey());
    }

    @Test
    public void equalsAndHashCodeUseKeyAndTags() {
        MetricId first = new MetricId("requests").tagged("region", "west");
        MetricId second = new MetricId("requests").tagged("region", "west");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void equalsReturnsFalseForDifferentKeyOrNull() {
        MetricId metricId = new MetricId("requests");

        assertFalse(metricId.equals(new MetricId("errors")));
        assertFalse(metricId.equals(null));
    }

    @Test
    public void compareToOrdersKeys() {
        MetricId first = new MetricId("alpha");
        MetricId second = new MetricId("beta");

        assertTrue(first.compareTo(second) < 0);
        assertTrue(second.compareTo(first) > 0);
    }

    @Test
    public void compareToReturnsZeroForEqualMetricIds() {
        MetricId first = new MetricId("requests").tagged("region", "west");
        MetricId second = new MetricId("requests").tagged("region", "west");

        assertEquals(0, first.compareTo(second));
    }

    @Test
    public void compareToNullReturnsNegativeOne() {
        assertEquals(-1, new MetricId("requests").compareTo(null));
    }

    @Test
    public void differentTagsProduceDifferentMetricIds() {
        MetricId first = new MetricId("requests").tagged("region", "west");
        MetricId second = new MetricId("requests").tagged("region", "east");

        assertNotEquals(first, second);
    }
}
