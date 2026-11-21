package org.rf.ide.core.execution.agent.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class Events_ArrowTest {

    @Test
    public void testToFileUriWithNull() {
        assertNull(Events.toFileUri(null));
    }

    @Test
    public void testToFileUriWithEmptyString() {
        assertNull(Events.toFileUri(""));
    }

    @Test
    public void testToFileUriWithValidPath() {
        URI result = Events.toFileUri("/path/to/file");
        assertEquals(URI.create("file:///path/to/file"), result);
    }

    @Test
    public void testToFileUriWithWindowsPath() {
        URI result = Events.toFileUri("C:\\path\\to\\file");
        assertEquals(URI.create("file:///C:/path/to/file"), result);
    }

    @Test
    public void testToFileUriWithSpecialCharacters() {
        URI result = Events.toFileUri("/path with spaces/file.txt");
        assertEquals(URI.create("file:///path%20with%20spaces/file.txt"), result);
    }

    @Test
    public void testEnsureListOfStrings() {
        List<?> input = Arrays.asList("one", "two", "three");
        List<String> result = Events.ensureListOfStrings(input);
        assertEquals(Arrays.asList("one", "two", "three"), result);
    }

    @Test
    public void testEnsureListOfStringsEmptyList() {
        List<?> input = Collections.emptyList();
        List<String> result = Events.ensureListOfStrings(input);
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void testEnsureListOfOrderedMapOfStringsToObjects() {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("key1", "value1");
        map1.put("key2", 42);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("key3", "value3");

        List<?> input = Arrays.asList(map1, map2);
        List<Map<String, Object>> result = Events.ensureListOfOrderedMapOfStringsToObjects(input);

        assertEquals(2, result.size());
        assertEquals("value1", result.get(0).get("key1"));
        assertEquals(42, result.get(0).get("key2"));
        assertEquals("value3", result.get(1).get("key3"));
    }

    @Test
    public void testEnsureOrderedMapOfStringsToObjects() {
        Map<String, Object> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", 42);

        Map<String, Object> result = Events.ensureOrderedMapOfStringsToObjects(input);

        assertEquals("value1", result.get("key1"));
        assertEquals(42, result.get("key2"));
    }

    @Test
    public void testEnsureOrderedMapOfStringsToStrings() {
        Map<String, String> input = new HashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");

        Map<String, String> result = Events.ensureOrderedMapOfStringsToStrings(input);

        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    @Test
    public void testEnsureOrderedMapOfStringsToStringsEmptyMap() {
        Map<String, String> input = Collections.emptyMap();
        Map<String, String> result = Events.ensureOrderedMapOfStringsToStrings(input);
        assertEquals(Collections.emptyMap(), result);
    }
}
