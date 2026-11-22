package com.avanza.astrix.core.util;

import com.avanza.astrix.core.AstrixRemoteResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenericAstrixMapReducer_ArrowTest {

    @Test
    public void reduceMergesMaps() {
        Map<String, Integer> first = new HashMap<String, Integer>();
        first.put("one", 1);
        first.put("shared", 2);

        Map<String, Integer> second = new HashMap<String, Integer>();
        second.put("two", 3);
        second.put("shared", 4);

        List<AstrixRemoteResult<Map<String, Integer>>> results =
                Arrays.<AstrixRemoteResult<Map<String, Integer>>>asList(
                        new AstrixRemoteResult<Map<String, Integer>>(first),
                        new AstrixRemoteResult<Map<String, Integer>>(second));

        Map<String, Integer> reduced = new GenericAstrixMapReducer<String, Integer>().reduce(results);

        Map<String, Integer> expected = new HashMap<String, Integer>();
        expected.put("one", 1);
        expected.put("two", 3);
        expected.put("shared", 4);
        assertEquals(expected, reduced);
    }

    @Test
    public void reduceEmptyResultsReturnsEmptyMap() {
        List<AstrixRemoteResult<Map<String, Integer>>> results =
                new ArrayList<AstrixRemoteResult<Map<String, Integer>>>();

        Map<String, Integer> reduced = new GenericAstrixMapReducer<String, Integer>().reduce(results);

        assertTrue(reduced.isEmpty());
    }
}
