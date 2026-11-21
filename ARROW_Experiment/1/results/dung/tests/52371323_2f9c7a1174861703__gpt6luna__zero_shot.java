package com.spotify.metrics.core;

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
    public void defaultConstructorHasNullKeyAndNoTags() {
        MetricId id = new MetricId();

        assertEquals(null, id.getKey());
        assertTrue(id.getTags().isEmpty());
        assertEquals("null {}", id.toString());
    }

    @Test
    public void keyConstructorPreservesKey() {
        MetricId id = new MetricId("requests");

        assertEquals("requests", id.getKey());
        assertTrue(id.getTags().isEmpty());
    }

    @Test
    public void mapConstructorCopiesAndSortsTags() {
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("z", "last");
        tags.put("a", "first");

        MetricId id = new MetricId("requests", tags);
        tags.put("new", "value");

        assertEquals("requests", id.getKey());
        assertEquals("{a=first, z=last}", id.getTags().toString());
        assertFalse(id.getTags().containsKey("new"));
    }

    @Test
    public void resolveAppendsPathAndInheritsTags() {
        MetricId original = new MetricId("service").tagged("region", "west");
        MetricId resolved = original.resolve("requests");

        assertEquals("service.requests", resolved.getKey());
        assertEquals(original.getTags(), resolved.getTags());
    }

    @Test
    public void resolveWithNullOrEmptyPartKeepsKey() {
        MetricId original = new MetricId("service");

        assertEquals("service", original.resolve(null).getKey());
        assertEquals("service", original.resolve("").getKey());
    }

    @Test
    public void resolveOnEmptyKeyUsesPart() {
        assertEquals("requests", new MetricId("").resolve("requests").getKey());
        assertEquals("requests", new MetricId().resolve("requests").getKey());
    }

    @Test
    public void taggedMapAddsAndOverridesTagsWithoutChangingOriginal() {
        MetricId original = new MetricId("requests").tagged("region", "west");
        Map<String, String> additions = new HashMap<String, String>();
        additions.put("region", "east");
        additions.put("host", "one");

        MetricId updated = original.tagged(additions);

        assertEquals("{host=one, region=east}", updated.getTags().toString());
        assertEquals("{region=west}", original.getTags().toString());
    }

    @Test
    public void taggedVarargsAddsPairs() {
        MetricId id = new MetricId("requests").tagged("region", "west", "host", "one");

        assertEquals("{host=one, region=west}", id.getTags().toString());
    }

    @Test
    public void taggedNullVarargsReturnsSameInstance() {
        MetricId id = new MetricId("requests");

        assertSame(id, id.tagged((String[]) null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void taggedOddNumberOfVarargsThrows() {
        new MetricId("requests").tagged("region");
    }

    @Test
    public void taggedSinglePairAddsTag() {
        MetricId id = new MetricId("requests").tagged("region", "west");

        assertEquals("{region=west}", id.getTags().toString());
    }

    @Test
    public void taggedTwoPairsAddsTags() {
        MetricId id = new MetricId("requests").tagged("region", "west", "host", "one");

        assertEquals("{host=one, region=west}", id.getTags().toString());
    }

    @Test
    public void taggedThreePairsAddsTags() {
        MetricId id = new MetricId("requests")
            .tagged("region", "west", "host", "one", "zone", "a");

        assertEquals("{host=one, region=west, zone=a}", id.getTags().toString());
    }

    @Test
    public void joinCombinesKeysAndTags() {
        MetricId joined = MetricId.join(
            new MetricId("service").tagged("region", "west"),
            new MetricId("").tagged("host", "one"),
            new MetricId("requests").tagged("region", "east"));

        assertEquals("service.requests", joined.getKey());
        assertEquals("{host=one, region=east}", joined.getTags().toString());
    }

    @Test
    public void buildWithNoPartsReturnsEmptyConstant() {
        assertSame(MetricId.EMPTY, MetricId.build());
        assertSame(MetricId.EMPTY, MetricId.build((String[]) null));
    }

    @Test
    public void buildWithOnePartPreservesIt() {
        MetricId id = MetricId.build("requests");

        assertEquals("requests", id.getKey());
        assertTrue(id.getTags().isEmpty());
    }

    @Test
    public void buildJoinsNonemptyParts() {
        MetricId id = MetricId.build("service", "", null, "requests");

        assertEquals("service.requests", id.getKey());
        assertTrue(id.getTags().isEmpty());
    }

    @Test
    public void equalsAndHashCodeUseKeyAndTags() {
        MetricId first = new MetricId("requests").tagged("region", "west");
        MetricId second = new MetricId("requests").tagged("region", "west");
        MetricId different = new MetricId("requests").tagged("region", "east");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(different));
        assertFalse(first.equals(null));
        assertFalse(first.equals("requests"));
    }

    @Test
    public void compareToOrdersKeysAndHandlesNull() {
        MetricId alpha = new MetricId("alpha");
        MetricId beta = new MetricId("beta");

        assertTrue(alpha.compareTo(beta) < 0);
        assertTrue(beta.compareTo(alpha) > 0);
        assertEquals(0, alpha.compareTo(new MetricId("alpha")));
        assertEquals(-1, alpha.compareTo(null));
    }

    @Test
    public void compareToOrdersTagsWhenKeysMatch() {
        MetricId first = new MetricId("requests").tagged("region", "east");
        MetricId second = new MetricId("requests").tagged("region", "west");

        assertTrue(first.compareTo(second) < 0);
        assertTrue(second.compareTo(first) > 0);
    }

    @Test
    public void tagsAreUnmodifiable() {
        MetricId id = new MetricId("requests").tagged("region", "west");

        try {
            id.getTags().put("host", "one");
        } catch (UnsupportedOperationException expected) {
            assertEquals("{region=west}", id.getTags().toString());
            return;
        }

        throw new AssertionError("Expected tags to be unmodifiable");
    }
}
