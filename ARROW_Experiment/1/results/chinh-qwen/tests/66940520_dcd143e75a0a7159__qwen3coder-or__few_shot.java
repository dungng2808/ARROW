package org.hisp.dhis.commons.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

public class CachingMap_ArrowTest {

    @Test
    public void testGetWithNullKeyReturnsNull() throws Exception {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "value";
            }
        };
        assertNull(map.get(null, callable));
    }

    @Test
    public void testGetCacheHit() throws Exception {
        CachingMap<String, String> map = new CachingMap<>();
        map.put("key1", "cachedValue");
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "newValue";
            }
        };
        assertEquals("cachedValue", map.get("key1", callable));
        assertEquals(1L, map.getCacheHitCount());
        assertEquals(0L, map.getCacheMissCount());
    }

    @Test
    public void testGetCacheMiss() throws Exception {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "newValue";
            }
        };
        assertEquals("newValue", map.get("key1", callable));
        assertEquals(0L, map.getCacheHitCount());
        assertEquals(1L, map.getCacheMissCount());
    }

    @Test
    public void testGetWithDefaultValue() throws Exception {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return null;
            }
        };
        assertEquals("defaultValue", map.get("key1", callable, "defaultValue"));
        assertEquals(0L, map.getCacheHitCount());
        assertEquals(1L, map.getCacheMissCount());
    }

    @Test
    public void testLoad() {
        CachingMap<String, String> map = new CachingMap<>();
        List<String> collection = new ArrayList<>();
        collection.add("item1");
        collection.add("item2");
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "newValue";
            }
        };
        map.get("key1", callable);
        map.load(collection, item -> "key_" + item);
        assertEquals(1L, map.getCacheLoadCount());
        assertTrue(map.isCacheLoaded());
    }

    @Test
    public void testLoadWithNullKey() {
        CachingMap<String, String> map = new CachingMap<>();
        List<String> collection = new ArrayList<>();
        collection.add("item1");
        collection.add("item2");
        map.load(collection, item -> null);
        assertEquals(1L, map.getCacheLoadCount());
        assertEquals(0, map.size());
    }

    @Test
    public void testGetCacheHitRatio() throws Exception {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "value";
            }
        };
        map.get("key1", callable);
        map.get("key1", callable);
        assertEquals(1.0, map.getCacheHitRatio(), 0.0);
    }

    @Test
    public void testToString() throws Exception {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "value";
            }
        };
        map.get("key1", callable);
        String result = map.toString();
        assertTrue(result.contains("Size: 1"));
        assertTrue(result.contains("Hit count: 0"));
        assertTrue(result.contains("Miss count: 1"));
        assertTrue(result.contains("Load count: 0"));
    }
}
