package org.hisp.dhis.commons.collection;

import java.util.Arrays;
import java.util.concurrent.Callable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CachingMap_ArrowTest {

    @Test
    public void getCachesCallableResultAndTracksHitsAndMisses() {
        CachingMap<String, String> map = new CachingMap<String, String>();
        Callable<String> callable = new Callable<String>() {
            public String call() {
                return "value";
            }
        };

        assertEquals("value", map.get("key", callable));
        assertEquals("value", map.get("key", callable));
        assertEquals(1L, map.getCacheMissCount());
        assertEquals(1L, map.getCacheHitCount());
        assertEquals(1.0, map.getCacheHitRatio(), 0.0);
    }

    @Test
    public void getWithNullKeyDoesNotCallCallable() {
        CachingMap<String, String> map = new CachingMap<String, String>();
        Callable<String> callable = new Callable<String>() {
            public String call() {
                throw new AssertionError();
            }
        };

        assertNull(map.get(null, callable));
        assertEquals(0L, map.getCacheHitCount());
        assertEquals(0L, map.getCacheMissCount());
    }

    @Test
    public void getCachesNullValuesAndReturnsDefaultWhenValueIsNull() {
        CachingMap<String, String> map = new CachingMap<String, String>();
        Callable<String> callable = new Callable<String>() {
            public String call() {
                return null;
            }
        };

        assertEquals("fallback", map.get("key", callable, "fallback"));
        assertEquals("fallback", map.get("key", callable, "fallback"));
        assertEquals(1L, map.getCacheMissCount());
        assertEquals(1L, map.getCacheHitCount());
    }

    @Test
    public void getWrapsCallableException() {
        CachingMap<String, String> map = new CachingMap<String, String>();
        Callable<String> callable = new Callable<String>() {
            public String call() throws Exception {
                throw new Exception("failure");
            }
        };

        try {
            map.get("key", callable);
        } catch (RuntimeException exception) {
            assertTrue(exception.getCause() instanceof Exception);
            assertEquals("failure", exception.getCause().getMessage());
            return;
        }
        throw new AssertionError("Expected RuntimeException");
    }

    @Test
    public void loadIgnoresNullKeysAndTracksLoads() {
        CachingMap<String, String> map = new CachingMap<String, String>();

        assertFalse(map.isCacheLoaded());
        assertSame(map, map.load(Arrays.asList("one", "skip", "two"), value ->
            "skip".equals(value) ? null : value));
        assertTrue(map.isCacheLoaded());
        assertEquals(1L, map.getCacheLoadCount());
        assertEquals(2, map.size());
        assertEquals("one", map.get("one", () -> "unexpected"));
    }

    @Test
    public void toStringReportsCacheStatistics() {
        CachingMap<String, String> map = new CachingMap<String, String>();
        map.get("key", () -> "value");
        map.get("key", () -> "other");

        assertEquals("[Size: 1, Hit count: 1, Miss count: 1, Hit ratio: 1.0, Load count: 0]",
            map.toString());
    }
}
