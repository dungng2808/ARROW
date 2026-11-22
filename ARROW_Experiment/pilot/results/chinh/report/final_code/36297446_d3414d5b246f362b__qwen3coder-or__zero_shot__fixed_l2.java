package com.avanza.astrix.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.avanza.astrix.core.AstrixRemoteResult;

public class GenericAstrixMapReducer_ArrowTest {

	@Test
	public void should_reduce_multiple_results_into_single_map() throws Exception {
		// Given
		GenericAstrixMapReducer<String, Integer> reducer = new GenericAstrixMapReducer<>();
		
		AstrixRemoteResult<Map<String, Integer>> result1 = new MockAstrixRemoteResult<>(createMap("key1", 1, "key2", 2));
		AstrixRemoteResult<Map<String, Integer>> result2 = new MockAstrixRemoteResult<>(createMap("key3", 3, "key4", 4));
		AstrixRemoteResult<Map<String, Integer>> result3 = new MockAstrixRemoteResult<>(createMap("key5", 5));
		
		List<AstrixRemoteResult<Map<String, Integer>>> results = new ArrayList<>();
		results.add(result1);
		results.add(result2);
		results.add(result3);
		
		// When
		Map<String, Integer> reduced = reducer.reduce(results);
		
		// Then
		assertNotNull(reduced);
		assertEquals(5, reduced.size());
		assertEquals(Integer.valueOf(1), reduced.get("key1"));
		assertEquals(Integer.valueOf(2), reduced.get("key2"));
		assertEquals(Integer.valueOf(3), reduced.get("key3"));
		assertEquals(Integer.valueOf(4), reduced.get("key4"));
		assertEquals(Integer.valueOf(5), reduced.get("key5"));
	}
	
	@Test
	public void should_reduce_empty_list_into_empty_map() throws Exception {
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
	public void should_handle_null_values_in_maps() throws Exception {
		// Given
		GenericAstrixMapReducer<String, Integer> reducer = new GenericAstrixMapReducer<>();
		
		AstrixRemoteResult<Map<String, Integer>> result1 = new MockAstrixRemoteResult<>(createMap("key1", null, "key2", 2));
		AstrixRemoteResult<Map<String, Integer>> result2 = new MockAstrixRemoteResult<>(createMap("key3", 3));
		
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
	
	private Map<String, Integer> createMap(String key1, Integer value1, String key2, Integer value2) {
		Map<String, Integer> map = new HashMap<>();
		map.put(key1, value1);
		map.put(key2, value2);
		return map;
	}
	
	private Map<String, Integer> createMap(String key1, Integer value1) {
		Map<String, Integer> map = new HashMap<>();
		map.put(key1, value1);
		return map;
	}
	
	private static class MockAstrixRemoteResult<K, V> implements AstrixRemoteResult<Map<K, V>> {
		private final Map<K, V> result;
		
		public MockAstrixRemoteResult(Map<K, V> result) {
			this.result = result;
		}
		
		@Override
		public Map<K, V> getResult() {
			return result;
		}
		
		@Override
		public boolean isSuccessful() {
			return true;
		}
		
		@Override
		public Throwable getFailure() {
			return null;
		}
	}
}
