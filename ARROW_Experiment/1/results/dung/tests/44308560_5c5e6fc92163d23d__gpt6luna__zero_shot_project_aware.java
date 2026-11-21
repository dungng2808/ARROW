package org.rf.ide.core.execution.agent.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class Events_ArrowTest {

    @Test
    public void toFileUriConvertsAbsolutePath() {
        assertEquals(URI.create("file:///tmp/example.robot"), Events.toFileUri("/tmp/example.robot"));
    }

    @Test
    public void toFileUriConvertsRelativePath() {
        assertEquals(URI.create("file:///tmp/example.robot"), Events.toFileUri("tmp/example.robot"));
    }

    @Test
    public void toFileUriEscapesSpacesAndNormalizesBackslashes() {
        assertEquals(URI.create("file:///tmp/a%20b.robot"), Events.toFileUri("tmp\\a b.robot"));
    }

    @Test
    public void toFileUriReturnsNullForNullOrEmptySource() {
        assertNull(Events.toFileUri(null));
        assertNull(Events.toFileUri(""));
    }

    @Test
    public void toFileUriReturnsNullForInvalidUri() {
        assertNull(Events.toFileUri("bad\npath"));
    }

    @Test
    public void ensureListOfStringsPreservesValuesAndOrder() {
        List<String> result = Events.ensureListOfStrings(Arrays.<Object>asList("first", "second"));
        assertEquals(Arrays.asList("first", "second"), result);
    }

    @Test
    public void ensureListOfStringsReturnsEmptyListForEmptyInput() {
        assertEquals(new ArrayList<String>(), Events.ensureListOfStrings(new ArrayList<Object>()));
    }

    @Test
    public void ensureListOfStringsRejectsNonStringValues() {
        assertThrows(ClassCastException.class,
                () -> Events.ensureListOfStrings(Arrays.<Object>asList("valid", Integer.valueOf(1))));
    }

    @Test
    public void ensureListOfOrderedMapOfStringsToObjectsPreservesMapsAndOrder() {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("one", Integer.valueOf(1));
        Map<String, Object> second = new LinkedHashMap<>();
        second.put("two", "value");

        List<Map<String, Object>> result = Events.ensureListOfOrderedMapOfStringsToObjects(
                Arrays.<Object>asList(first, second));

        assertEquals(Arrays.asList(first, second), result);
        assertEquals(Arrays.asList("one"), new ArrayList<>(result.get(0).keySet()));
        assertEquals(Arrays.asList("two"), new ArrayList<>(result.get(1).keySet()));
    }

    @Test
    public void ensureListOfOrderedMapOfStringsToObjectsReturnsEmptyListForEmptyInput() {
        assertEquals(new ArrayList<Map<String, Object>>(),
                Events.ensureListOfOrderedMapOfStringsToObjects(new ArrayList<Object>()));
    }

    @Test
    public void ensureListOfOrderedMapOfStringsToObjectsRejectsNonMapValues() {
        assertThrows(ClassCastException.class,
                () -> Events.ensureListOfOrderedMapOfStringsToObjects(Arrays.<Object>asList("not a map")));
    }

    @Test
    public void ensureOrderedMapOfStringsToObjectsPreservesOrderAndValues() {
        Map<Object, Object> input = new LinkedHashMap<>();
        input.put("first", Integer.valueOf(1));
        input.put("second", null);

        Map<String, Object> result = Events.ensureOrderedMapOfStringsToObjects(input);

        assertEquals(Integer.valueOf(1), result.get("first"));
        assertNull(result.get("second"));
        assertEquals(Arrays.asList("first", "second"), new ArrayList<>(result.keySet()));
    }

    @Test
    public void ensureOrderedMapOfStringsToObjectsRejectsNonStringKeys() {
        Map<Object, Object> input = new LinkedHashMap<>();
        input.put(Integer.valueOf(1), "value");

        assertThrows(ClassCastException.class, () -> Events.ensureOrderedMapOfStringsToObjects(input));
    }

    @Test
    public void ensureOrderedMapOfStringsToStringsPreservesOrderAndValues() {
        Map<Object, Object> input = new LinkedHashMap<>();
        input.put("first", "one");
        input.put("second", "two");

        Map<String, String> result = Events.ensureOrderedMapOfStringsToStrings(input);

        assertEquals("one", result.get("first"));
        assertEquals("two", result.get("second"));
        assertEquals(Arrays.asList("first", "second"), new ArrayList<>(result.keySet()));
    }

    @Test
    public void ensureOrderedMapOfStringsToStringsAllowsNullValues() {
        Map<Object, Object> input = new LinkedHashMap<>();
        input.put("key", null);

        Map<String, String> result = Events.ensureOrderedMapOfStringsToStrings(input);

        assertEquals(1, result.size());
        assertNull(result.get("key"));
    }

    @Test
    public void ensureOrderedMapOfStringsToStringsRejectsNonStringValues() {
        Map<Object, Object> input = new LinkedHashMap<>();
        input.put("key", Integer.valueOf(1));

        assertThrows(ClassCastException.class, () -> Events.ensureOrderedMapOfStringsToStrings(input));
    }
}
