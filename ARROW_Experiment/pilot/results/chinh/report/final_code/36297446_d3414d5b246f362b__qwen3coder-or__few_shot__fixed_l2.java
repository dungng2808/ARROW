package com.avanza.astrix.core.util;

import com.avanza.astrix.core.AstrixRemoteResult;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GenericAstrixMapReducer_ArrowTest {

    @Test
    public void should_reduce_maps_into_one_map() {
        // Given
        GenericAstrixMapReducer<String, Integer> reducer = new GenericAstrixMapReducer<>();
        
        AstrixRemoteResult<Map<String, Integer>> result1 = new MockAstrixRemoteResult<>(createMap("key1", 1, "key2", 2));
        AstrixRemoteResult<Map<String, Integer>> result2 = new MockAstrixRemoteResult<>(createMap("key3", 3, "key4", 4));
        
        List<AstrixRemoteResult<Map<String, Integer>>> results = new ArrayList<>();
        results.add(result1);
        results.add(result2);
        
        // When
        Map<String, Integer> reduced = reducer.reduce(results);
        
        // Then
        assertNotNull(reduced);
        assertEquals(4, reduced.size());
        assertEquals(Integer.valueOf(1), reduced.get("key1"));
        assertEquals(Integer.valueOf(2), reduced.get("key2"));
        assertEquals(Integer.valueOf(3), reduced.get("key3"));
        assertEquals(Integer.valueOf(4), reduced.get("key4"));
    }

    @Test
    public void should_handle_empty_results_list() {
        // Given
        GenericAstrixMapReducer<String, Integer> reducer = new GenericAstrixMapReducer<>();
        List<AstrixRemoteResult<Map<String, Integer>>> results = new ArrayList<>();
        
        // When
        Map<String, Integer> reduced = reducer.reduce(results);
        
        // Then
        assertNotNull(reduced);
        assertEquals(0, reduced.size());
    }

    @Test
    public void should_handle_null_values_in_map() {
        // Given
        GenericAstrixMapReducer<String, Integer> reducer = new GenericAstrixMapReducer<>();
        
        AstrixRemoteResult<Map<String, Integer>> result1 = new MockAstrixRemoteResult<>(createMap("key1", null, "key2", "2"));
        AstrixRemoteResult<Map<String, Integer>> result2 = new MockAstrixRemoteResult<>(createMap("key3", "3"));
        
        List<AstrixRemoteResult<Map<String, Integer>>> results = new ArrayList<>();
        results.add(result1);
        results.add(result2);
        
        // When
        Map<String, Integer> reduced = reducer.reduce(results);
        
        // Then
        assertNotNull(reduced);
        assertEquals(3, reduced.size());
        assertEquals(null, reduced.get("key1"));
        assertEquals(Integer.valueOf(2), reduced.get("key2"));
        assertEquals(Integer.valueOf(3), reduced.get("key3"));
    }

    @Test
    public void should_handle_overlapping_keys() {
        // Given
        GenericAstrixMapReducer<String, Integer> reducer = new GenericAstrixMapReducer<>();
        
        AstrixRemoteResult<Map<String, Integer>> result1 = new MockAstrixRemoteResult<>(createMap("key1", "1", "key2", "2"));
        AstrixRemoteResult<Map<String, Integer>> result2 = new MockAstrixRemoteResult<>(createMap("key2", "20", "key3", "3"));
        
        List<AstrixRemoteResult<Map<String, Integer>>> results = new ArrayList<>();
        results.add(result1);
        results.add(result2);
        
        // When
        Map<String, Integer> reduced = reducer.reduce(results);
        
        // Then
        assertNotNull(reduced);
        assertEquals(3, reduced.size());
        assertEquals(Integer.valueOf(1), reduced.get("key1"));
        assertEquals(Integer.valueOf(20), reduced.get("key2")); // Overwritten by second result
        assertEquals(Integer.valueOf(3), reduced.get("key3"));
    }

    private Map<String, Integer> createMap(String... keyValuePairs) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            String key = keyValuePairs[i];
            Integer value = null;
            if (keyValuePairs[i + 1] != null) {
                value = Integer.parseInt(keyValuePairs[i + 1]);
            }
            map.put(key, value);
        }
        return map;
    }

    private static class MockAstrixRemoteResult<V> implements AstrixRemoteResult<V> {
        private final V result;

        public MockAstrixRemoteResult(V result) {
            this.result = result;
        }

        @Override
        public V getResult() {
            return result;
        }

        @Override
        public Throwable getThrowable() {
            return null;
        }

        @Override
        public boolean isSuccessful() {
            return true;
        }
    }
}
