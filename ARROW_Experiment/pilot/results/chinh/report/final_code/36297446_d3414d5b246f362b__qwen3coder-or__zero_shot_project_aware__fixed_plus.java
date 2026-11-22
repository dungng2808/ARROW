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
	public void should_reduce_empty_list() {
		// Given
		GenericAstrixMapReducer<String, String> reducer = new GenericAstrixMapReducer<String, String>();
		List<AstrixRemoteResult<Map<String, String>>> results = new ArrayList<AstrixRemoteResult<Map<String, String>>>();
		
		// When
		Map<String, String> reduced = reducer.reduce(results);
		
		// Then
		assertNotNull(reduced);
		assertEquals(0, reduced.size());
	}
	
	@Test
	public void should_reduce_single_result() {
		// Given
		GenericAstrixMapReducer<String, String> reducer = new GenericAstrixMapReducer<String, String>();
		List<AstrixRemoteResult<Map<String, String>>> results = new ArrayList<AstrixRemoteResult<Map<String, String>>>();
		
		Map<String, String> singleMap = new HashMap<String, String>();
		singleMap.put("key1", "value1");
		singleMap.put("key2", "value2");
		
		results.add(new MockAstrixRemoteResult<Map<String, String>>(singleMap));
		
		// When
		Map<String, String> reduced = reducer.reduce(results);
		
		// Then
		assertNotNull(reduced);
		assertEquals(2, reduced.size());
		assertEquals("value1", reduced.get("key1"));
		assertEquals("value2", reduced.get("key2"));
	}
	
	@Test
	public void should_reduce_multiple_results() {
		// Given
		GenericAstrixMapReducer<String, String> reducer = new GenericAstrixMapReducer<String, String>();
		List<AstrixRemoteResult<Map<String, String>>> results = new ArrayList<AstrixRemoteResult<Map<String, String>>>();
		
		Map<String, String> map1 = new HashMap<String, String>();
		map1.put("key1", "value1");
		map1.put("key2", "value2");
		
		Map<String, String> map2 = new HashMap<String, String>();
		map2.put("key3", "value3");
		map2.put("key4", "value4");
		
		Map<String, String> map3 = new HashMap<String, String>();
		map3.put("key2", "overriddenValue");
		map3.put("key5", "value5");
		
		results.add(new MockAstrixRemoteResult<Map<String, String>>(map1));
		results.add(new MockAstrixRemoteResult<Map<String, String>>(map2));
		results.add(new MockAstrixRemoteResult<Map<String, String>>(map3));
		
		// When
		Map<String, String> reduced = reducer.reduce(results);
		
		// Then
		assertNotNull(reduced);
		assertEquals(4, reduced.size());
		assertEquals("value1", reduced.get("key1"));
		assertEquals("overriddenValue", reduced.get("key2")); // overridden by last map
		assertEquals("value3", reduced.get("key3"));
		assertEquals("value5", reduced.get("key5"));
	}
	
	private static class MockAstrixRemoteResult<T> implements AstrixRemoteResult<T> {
		private final T result;
		
		public MockAstrixRemoteResult(T result) {
			this.result = result;
		}
		
		public T getResult() {
			return result;
		}
		
		public boolean isSuccessful() {
			return true;
		}
		
		public Throwable getFailure() {
			return null;
		}
	}
}
