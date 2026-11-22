package com.avanza.astrix.core.util;

import static org.junit.Assert.assertEquals;

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
		first.put("two", 2);

		Map<String, Integer> second = new HashMap<String, Integer>();
		second.put("three", 3);

		List<AstrixRemoteResult<Map<String, Integer>>> results =
				new ArrayList<AstrixRemoteResult<Map<String, Integer>>>();
		results.add(AstrixRemoteResult.successful(first));
		results.add(AstrixRemoteResult.successful(second));

		Map<String, Integer> expected = new HashMap<String, Integer>();
		expected.put("one", 1);
		expected.put("two", 2);
		expected.put("three", 3);

		assertEquals(expected, new GenericAstrixMapReducer<String, Integer>().reduce(results));
	}

	@Test
	public void laterResultsOverwriteEarlierValuesForDuplicateKeys() {
		Map<String, Integer> first = new HashMap<String, Integer>();
		first.put("key", 1);

		Map<String, Integer> second = new HashMap<String, Integer>();
		second.put("key", 2);

		List<AstrixRemoteResult<Map<String, Integer>>> results =
				Arrays.asList(AstrixRemoteResult.successful(first),
						AstrixRemoteResult.successful(second));

		Map<String, Integer> expected = new HashMap<String, Integer>();
		expected.put("key", 2);

		assertEquals(expected, new GenericAstrixMapReducer<String, Integer>().reduce(results));
	}

	@Test
	public void reduceEmptyResultsReturnsEmptyMap() {
		List<AstrixRemoteResult<Map<String, Integer>>> results =
				new ArrayList<AstrixRemoteResult<Map<String, Integer>>>();

		assertEquals(new HashMap<String, Integer>(),
				new GenericAstrixMapReducer<String, Integer>().reduce(results));
	}
}
