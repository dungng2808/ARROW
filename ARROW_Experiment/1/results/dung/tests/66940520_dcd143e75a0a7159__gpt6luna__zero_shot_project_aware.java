package org.hisp.dhis.commons.collection;

import java.util.Arrays;
import java.util.Collections;
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
        assertEquals(1.0, map.getCacheHitRatio(), 0.0);
    }

    @Test
    public void getWithNullKeyDoesNotCallCallable()
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
    public void getCachesNullValueAndDefaultOverloadReturnsDefault()
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

        assertEquals("fallback", map.get("key", callable, "fallback"));
        assertEquals("fallback", map.get("key", new Callable<String>()
        {
            @Override
            public String call()
            {
                return "unexpected";
            }
        }, "fallback"));
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
                    throw new Exception("failure");
                }
            });
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getCause() instanceof Exception);
            assertEquals("failure", ex.getCause().getMessage());
            return;
        }

        throw new AssertionError("Expected RuntimeException");
    }

    @Test
    public void loadIgnoresNullKeysAndReturnsSameMap()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();

        CachingMap<String, String> result = map.load(Arrays.asList("one", "two"), new java.util.function.Function<String, String>()
        {
            @Override
            public String apply(String value)
            {
                return "one".equals(value) ? "key-one" : null;
            }
        });

        assertSame(map, result);
        assertEquals(1, map.size());
        assertEquals("one", map.get("key-one"));
        assertFalse(map.isCacheLoaded() == false);
        assertEquals(1L, map.getCacheLoadCount());
    }

    @Test
    public void loadEmptyCollectionIncrementsLoadCount()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();

        map.load(Collections.<String>emptyList(), new java.util.function.Function<String, String>()
        {
            @Override
            public String apply(String value)
            {
                return value;
            }
        });

        assertTrue(map.isCacheLoaded());
        assertEquals(1L, map.getCacheLoadCount());
    }

    @Test
    public void hitRatioIsNanBeforeGetsAndInfinityWithHitsOnly()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        assertTrue(Double.isNaN(map.getCacheHitRatio()));

        map.put("key", "value");
        assertEquals("value", map.get("key", new Callable<String>()
        {
            @Override
            public String call()
            {
                return "unused";
            }
        }));
        assertEquals(Double.POSITIVE_INFINITY, map.getCacheHitRatio(), 0.0);
    }

    @Test
    public void toStringIncludesSizeAndCounters()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        map.put("key", "value");

        assertEquals("[Size: 1, Hit count: 0, Miss count: 0, Hit ratio: NaN, Load count: 0]", map.toString());
    }
}
