package org.hisp.dhis.commons.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import junit.framework.TestCase;

public class CachingMap_ArrowTest extends TestCase
{
    public void testGetWithNullKey()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "value";
            }
        };
        assertNull( map.get( null, callable ) );
    }
    
    public void testGetCacheHit()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        map.put( "key", "cachedValue" );
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "newValue";
            }
        };
        assertEquals( "cachedValue", map.get( "key", callable ) );
        assertEquals( 1L, map.getCacheHitCount() );
        assertEquals( 0L, map.getCacheMissCount() );
    }
    
    public void testGetCacheMiss()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "newValue";
            }
        };
        assertEquals( "newValue", map.get( "key", callable ) );
        assertEquals( 0L, map.getCacheHitCount() );
        assertEquals( 1L, map.getCacheMissCount() );
        assertTrue( map.containsKey( "key" ) );
        assertEquals( "newValue", map.get( "key" ) );
    }
    
    public void testGetWithDefaultValue()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return null;
            }
        };
        assertEquals( "defaultValue", map.get( "key", callable, "defaultValue" ) );
        assertEquals( 0L, map.getCacheHitCount() );
        assertEquals( 1L, map.getCacheMissCount() );
    }
    
    public void testLoad()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        List<String> collection = new ArrayList<String>();
        collection.add( "item1" );
        collection.add( "item2" );
        collection.add( "item3" );
        Map<String, String> expected = new HashMap<String, String>();
        expected.put( "item1", "item1" );
        expected.put( "item2", "item2" );
        expected.put( "item3", "item3" );
        map.load( collection, new java.util.function.Function<String, String>()
        {
            @Override
            public String apply( String item )
            {
                return item;
            }
        } );
        assertEquals( 1L, map.getCacheLoadCount() );
        assertTrue( map.isCacheLoaded() );
        assertEquals( expected.size(), map.size() );
        for ( String key : expected.keySet() )
        {
            assertEquals( expected.get( key ), map.get( key ) );
        }
    }
    
    public void testLoadWithNullKey()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        List<String> collection = new ArrayList<String>();
        collection.add( "item1" );
        collection.add( null );
        collection.add( "item3" );
        map.load( collection, new java.util.function.Function<String, String>()
        {
            @Override
            public String apply( String item )
            {
                return item;
            }
        } );
        assertEquals( 1L, map.getCacheLoadCount() );
        assertTrue( map.isCacheLoaded() );
        assertEquals( 2, map.size() );
        assertTrue( map.containsKey( "item1" ) );
        assertTrue( map.containsKey( "item3" ) );
        assertFalse( map.containsKey( null ) );
    }
    
    public void testGetCacheHitRatio()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "value";
            }
        };
        // Cache miss
        map.get( "key1", callable );
        // Cache hit
        map.get( "key1", callable );
        // Cache hit
        map.get( "key1", callable );
        assertEquals( 2.0, map.getCacheHitRatio(), 0.0001 );
    }
    
    public void testToString()
    {
        CachingMap<String, String> map = new CachingMap<String, String>();
        Callable<String> callable = new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return "value";
            }
        };
        map.get( "key1", callable );
        String result = map.toString();
        assertTrue( result.contains( "Size: 1" ) );
        assertTrue( result.contains( "Hit count: 0" ) );
        assertTrue( result.contains( "Miss count: 1" ) );
        assertTrue( result.contains( "Load count: 0" ) );
    }
}
