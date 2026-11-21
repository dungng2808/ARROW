package org.hisp.dhis.commons.collection;

import java.util.Arrays;
import java.util.concurrent.Callable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CachingMap_ArrowTest
{
    @Test
    public void getLoadsAndCachesValue()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call()
            {
                return "value";
            }
        };

        assertEquals("value", map.get("key", callable));
        assertEquals("value", map.get("key", new Callable<String>()
        {
            @Override
            public String call()
            {
                return "other";
            }
        }));
        assertEquals(1L, map.getCacheMissCount());
        assertEquals(1L, map.getCacheHitCount());
    }

    @Test
    public void getNullKeyDoesNotCallCallable()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();

        assertNull(map.get(null, new Callable<String>()
        {
            @Override
            public String call()
            {
                throw new AssertionError();
            }
        }));
        assertEquals(0L, map.getCacheMissCount());
        assertEquals(0L, map.getCacheHitCount());
    }

    @Test
    public void getCachesNullAndReturnsDefaultWhenRequested()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call()
            {
                return null;
            }
        };

        assertEquals("default", map.get("key", callable, "default"));
        assertEquals("default", map.get("key", new Callable<String>()
        {
            @Override
            public String call()
            {
                return "unexpected";
            }
        }, "default"));
        assertEquals(1L, map.getCacheMissCount());
        assertEquals(1L, map.getCacheHitCount());
    }

    @Test
    public void getWrapsCallableException()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();

        try
        {
            map.get("key", new Callable<String>()
            {
                @Override
                public String call() throws Exception
                {
                    throw new Exception();
                }
            });
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getCause() instanceof Exception);
            assertEquals(0L, map.getCacheMissCount());
            return;
        }

        throw new AssertionError("Expected RuntimeException");
    }

    @Test
    public void loadIgnoresNullKeysAndTracksLoads()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();

        assertFalse(map.isCacheLoaded());
        assertSame(map, map.load(Arrays.asList("one", "skip", "two"), value ->
            "skip".equals(value) ? null : value));
        assertTrue(map.isCacheLoaded());
        assertEquals(1L, map.getCacheLoadCount());
        assertEquals(2, map.size());
        assertEquals("one", map.get("one", () -> "unexpected"));
        assertEquals(0L, map.getCacheMissCount());
        assertEquals(1L, map.getCacheHitCount());
    }

    @Test
    public void hitRatioAndStringReflectCounters()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        map.get("key", () -> "value");
        map.get("key", () -> "unused");

        assertEquals(1.0, map.getCacheHitRatio(), 0.0);
        assertEquals("[Size: 1, Hit count: 1, Miss count: 1, Hit ratio: 1.0, Load count: 0]",
            map.toString());
    }

    @Test
    public void hitRatioIsNanBeforeGets()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();

        assertTrue(Double.isNaN(map.getCacheHitRatio()));
    }
}
