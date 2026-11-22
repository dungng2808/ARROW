package com.avanza.astrix.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.avanza.astrix.core.AstrixRemoteResult;

public class GenericAstrixMapReducer_ArrowTest {

	@Test
	public void reduceCombinesEntriesFromAllResults() {
		Map<String, Integer> first = new HashMap<String, Integer>();
		first.put("one", 1);
		first.put("shared", 2);

		Map<String, Integer> second = new HashMap<String, Integer>();
		second.put("two", 3);
		second.put("shared", 4);

		List<AstrixRemoteResult<Map<String, Integer>>> results =
				new ArrayList<AstrixRemoteResult<Map<String, Integer>>>();
		results.add(AstrixRemoteResult.successful(first));
		results.add(AstrixRemoteResult.successful(second));

		Map<String, Integer> expected = new HashMap<String, Integer>();
		expected.put("one", 1);
		expected.put("two", 3);
		expected.put("shared", 4);

		assertEquals(expected, new GenericAstrixMapReducer<String, Integer>().reduce(results));
	}

	@Test
	public void reduceOfEmptyResultsReturnsEmptyMap() {
		List<AstrixRemoteResult<Map<String, Integer>>> results =
				new ArrayList<AstrixRemoteResult<Map<String, Integer>>>();

		Map<String, Integer> reduced = new GenericAstrixMapReducer<String, Integer>().reduce(results);

		assertTrue(reduced.isEmpty());
	}

	@Test
	public void reduceOfResultsContainingEmptyMapReturnsEmptyMap() {
		Map<String, Integer> empty = new HashMap<String, Integer>();
		List<AstrixRemoteResult<Map<String, Integer>>> results =
				Arrays.asList(AstrixRemoteResult.successful(empty));

		assertTrue(new GenericAstrixMapReducer<String, Integer>().reduce(results).isEmpty());
	}
}
