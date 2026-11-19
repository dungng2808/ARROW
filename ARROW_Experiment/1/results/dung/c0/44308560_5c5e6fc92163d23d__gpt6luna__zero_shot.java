package org.rf.ide.core.execution.agent.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class Events_ArrowTest {

    @Test
    public void toFileUriConvertsRelativePath() {
        assertEquals(URI.create("file:///folder/file.robot"), Events.toFileUri("folder/file.robot"));
    }

    @Test
    public void toFileUriPreservesAbsolutePath() {
        assertEquals(URI.create("file:///folder/file.robot"), Events.toFileUri("/folder/file.robot"));
    }

    @Test
    public void toFileUriConvertsBackslashes() {
        assertEquals(URI.create("file:///folder/file.robot"), Events.toFileUri("folder\\file.robot"));
    }

    @Test
    public void toFileUriReturnsNullForNullOrEmptySource() {
        assertNull(Events.toFileUri(null));
        assertNull(Events.toFileUri(""));
    }

    @Test
    public void ensureListOfStringsPreservesValuesAndOrder() {
        List<String> result = Events.ensureListOfStrings(Arrays.<Object>asList("first", "second"));
        assertEquals(Arrays.asList("first", "second"), result);
    }

    @Test
    public void ensureListOfStringsHandlesEmptyList() {
        assertEquals(new ArrayList<String>(), Events.ensureListOfStrings(new ArrayList<Object>()));
    }

    @Test
    public void ensureListOfOrderedMapOfStringsToObjectsPreservesOrder() {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("a", 1);
        first.put("b", "two");

        Map<String, Object> second = new LinkedHashMap<>();
        second.put("c", true);

        List<Map<String, Object>> result = Events.ensureListOfOrderedMapOfStringsToObjects(
                Arrays.<Object>asList(first, second));

        List<Map<String, Object>> expected = new ArrayList<>();
        expected.add(first);
        expected.add(second);
        assertEquals(expected, result);
        assertEquals(Arrays.asList("a", "b"), new ArrayList<String>(result.get(0).keySet()));
    }

    @Test
    public void ensureListOfOrderedMapOfStringsToObjectsHandlesEmptyList() {
        assertEquals(new ArrayList<Map<String, Object>>(),
                Events.ensureListOfOrderedMapOfStringsToObjects(new ArrayList<Object>()));
    }

    @Test
    public void ensureOrderedMapOfStringsToObjectsPreservesOrderAndValues() {
        Map<Object, Object> input = new LinkedHashMap<>();
        input.put("first", 1);
        input.put("second", "value");

        Map<String, Object> result = Events.ensureOrderedMapOfStringsToObjects(input);

        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("first", 1);
        expected.put("second", "value");
        assertEquals(expected, result);
        assertEquals(Arrays.asList("first", "second"), new ArrayList<String>(result.keySet()));
    }

    @Test
    public void ensureOrderedMapOfStringsToObjectsHandlesEmptyMap() {
        assertEquals(new LinkedHashMap<String, Object>(),
                Events.ensureOrderedMapOfStringsToObjects(new LinkedHashMap<Object, Object>()));
    }

    @Test
    public void ensureOrderedMapOfStringsToStringsPreservesOrderAndValues() {
        Map<Object, Object> input = new LinkedHashMap<>();
        input.put("first", "one");
        input.put("second", "two");

        Map<String, String> result = Events.ensureOrderedMapOfStringsToStrings(input);

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("first", "one");
        expected.put("second", "two");
        assertEquals(expected, result);
        assertEquals(Arrays.asList("first", "second"), new ArrayList<String>(result.keySet()));
    }

    @Test
    public void ensureOrderedMapOfStringsToStringsHandlesEmptyMap() {
        assertEquals(new LinkedHashMap<String, String>(),
                Events.ensureOrderedMapOfStringsToStrings(new LinkedHashMap<Object, Object>()));
    }
}
