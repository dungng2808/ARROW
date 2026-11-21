package org.rf.ide.core.execution.agent.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
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
        String source = "/path/to/file";
        URI result = Events.toFileUri(source);
        assertEquals(URI.create("file:///path/to/file"), result);
    }

    @Test
    public void testToFileUriWithWindowsPath() {
        String source = "C:\\path\\to\\file";
        URI result = Events.toFileUri(source);
        assertEquals(URI.create("file:///C:/path/to/file"), result);
    }

    @Test
    public void testToFileUriWithSpecialCharacters() {
        String source = "/path/with space/file";
        URI result = Events.toFileUri(source);
        assertEquals(URI.create("file:///path/with%20space/file"), result);
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
        Map<?, ?> map1 = new LinkedHashMap<>();
        map1.put("key1", "value1");
        Map<?, ?> map2 = new LinkedHashMap<>();
        map2.put("key2", "value2");
        List<?> input = Arrays.asList(map1, map2);
        List<Map<String, Object>> result = Events.ensureListOfOrderedMapOfStringsToObjects(input);
        assertEquals(2, result.size());
        assertEquals("value1", result.get(0).get("key1"));
        assertEquals("value2", result.get(1).get("key2"));
    }

    @Test
    public void testEnsureOrderedMapOfStringsToObjects() {
        Map<?, ?> input = new LinkedHashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");
        Map<String, Object> result = Events.ensureOrderedMapOfStringsToObjects(input);
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    @Test
    public void testEnsureOrderedMapOfStringsToStrings() {
        Map<?, ?> input = new LinkedHashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");
        Map<String, String> result = Events.ensureOrderedMapOfStringsToStrings(input);
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }
}
