package org.hisp.dhis.commons.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.junit.Test;
import static org.junit.Assert.*;

public class CachingMap_ArrowTest
{
    @Test
    public void testGetWithNullKeyReturnsNull()
    {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "value";
            }
        };
        
        assertNull(map.get(null, callable));
    }
    
    @Test
    public void testGetCacheMiss()
    {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "value";
            }
        };
        
        String result = map.get("key", callable);
        
        assertEquals("value", result);
        assertEquals(0L, map.getCacheHitCount());
        assertEquals(1L, map.getCacheMissCount());
        assertEquals(0L, map.getCacheLoadCount());
    }
    
    @Test
    public void testGetCacheHit()
    {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "value";
            }
        };
        
        // First call - cache miss
        String result1 = map.get("key", callable);
        assertEquals("value", result1);
        assertEquals(0L, map.getCacheHitCount());
        assertEquals(1L, map.getCacheMissCount());
        
        // Second call - cache hit
        String result2 = map.get("key", callable);
        assertEquals("value", result2);
        assertEquals(1L, map.getCacheHitCount());
        assertEquals(1L, map.getCacheMissCount());
    }
    
    @Test
    public void testGetWithDefaultValue()
    {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return null;
            }
        };
        
        String result = map.get("key", callable, "default");
        assertEquals("default", result);
    }
    
    @Test
    public void testGetWithCallableThrowingException()
    {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                throw new Exception("test exception");
            }
        };
        
        try
        {
            map.get("key", callable);
            fail("Expected RuntimeException to be thrown");
        }
        catch (RuntimeException e)
        {
            assertEquals("test exception", e.getCause().getMessage());
        }
    }
    
    @Test
    public void testLoad()
    {
        CachingMap<Integer, String> map = new CachingMap<>();
        Collection<String> collection = new ArrayList<>();
        collection.add("item1");
        collection.add("item2");
        
        Function<String, Integer> keyMapper = new Function<String, Integer>()
        {
            @Override
            public Integer apply(String item)
            {
                if ("item1".equals(item))
                {
                    return 1;
                }
                else if ("item2".equals(item))
                {
                    return 2;
                }
                return null;
            }
        };
        
        CachingMap<Integer, String> result = map.load(collection, keyMapper);
        
        assertSame(map, result);
        assertEquals(1, map.size());
        assertEquals("item1", map.get(1));
        assertEquals(1L, map.getCacheLoadCount());
    }
    
    @Test
    public void testLoadWithNullKey()
    {
        CachingMap<Integer, String> map = new CachingMap<>();
        Collection<String> collection = new ArrayList<>();
        collection.add("item1");
        collection.add("item2");
        
        Function<String, Integer> keyMapper = new Function<String, Integer>()
        {
            @Override
            public Integer apply(String item)
            {
                if ("item1".equals(item))
                {
                    return 1;
                }
                else if ("item2".equals(item))
                {
                    return null; // This should be ignored
                }
                return null;
            }
        };
        
        map.load(collection, keyMapper);
        
        assertEquals(1, map.size());
        assertEquals("item1", map.get(1));
        assertEquals(1L, map.getCacheLoadCount());
    }
    
    @Test
    public void testGetCacheHitCount()
    {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "value";
            }
        };
        
        map.get("key1", callable);
        map.get("key1", callable);
        map.get("key1", callable);
        
        assertEquals(2L, map.getCacheHitCount());
    }
    
    @Test
    public void testGetCacheMissCount()
    {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "value";
            }
        };
        
        map.get("key1", callable);
        map.get("key2", callable);
        map.get("key1", callable);
        
        assertEquals(2L, map.getCacheMissCount());
    }
    
    @Test
    public void testGetCacheHitRatio()
    {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "value";
            }
        };
        
        // 2 misses, 1 hit
        map.get("key1", callable);
        map.get("key2", callable);
        map.get("key1", callable);
        
        assertEquals(0.5, map.getCacheHitRatio(), 0.0001);
    }
    
    @Test
    public void testGetCacheLoadCount()
    {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "value";
            }
        };
        
        map.get("key1", callable);
        map.load(new ArrayList<String>(), new Function<String, String>()
        {
            @Override
            public String apply(String item)
            {
                return null;
            }
        });
        map.load(new ArrayList<String>(), new Function<String, String>()
        {
            @Override
            public String apply(String item)
            {
                return null;
            }
        });
        
        assertEquals(2L, map.getCacheLoadCount());
    }
    
    @Test
    public void testIsCacheLoaded()
    {
        CachingMap<String, String> map = new CachingMap<>();
        
        assertFalse(map.isCacheLoaded());
        
        map.load(new ArrayList<String>(), new Function<String, String>()
        {
            @Override
            public String apply(String item)
            {
                return null;
            }
        });
        
        assertTrue(map.isCacheLoaded());
    }
    
    @Test
    public void testToString()
    {
        CachingMap<String, String> map = new CachingMap<>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "value";
            }
        };
        
        map.get("key1", callable);
        map.get("key1", callable);
        
        String result = map.toString();
        assertTrue(result.contains("Size: 1"));
        assertTrue(result.contains("Hit count: 1"));
        assertTrue(result.contains("Miss count: 1"));
        assertTrue(result.contains("Load count: 0"));
    }
}
