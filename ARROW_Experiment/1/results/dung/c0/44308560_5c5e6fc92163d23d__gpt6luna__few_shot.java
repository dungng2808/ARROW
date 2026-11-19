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
        URI result = Events.toFileUri("folder/file.robot");
        assertEquals(URI.create("file:///folder/file.robot"), result);
    }

    @Test
    public void toFileUriPreservesAbsolutePath() {
        URI result = Events.toFileUri("/folder/file.robot");
        assertEquals(URI.create("file:///folder/file.robot"), result);
    }

    @Test
    public void toFileUriEscapesSpaces() {
        URI result = Events.toFileUri("folder/my file.robot");
        assertEquals(URI.create("file:///folder/my%20file.robot"), result);
    }

    @Test
    public void toFileUriReturnsNullForNullOrEmptySource() {
        assertNull(Events.toFileUri(null));
        assertNull(Events.toFileUri(""));
    }

    @Test
    public void toFileUriReturnsNullForInvalidUri() {
        assertNull(Events.toFileUri("folder/file\n.robot"));
    }

    @Test
    public void ensureListOfStringsPreservesValues() {
        List<String> input = Arrays.asList("first", "second");
        assertEquals(input, Events.ensureListOfStrings(input));
    }

    @Test
    public void ensureListOfStringsReturnsEmptyListForEmptyInput() {
        assertEquals(new ArrayList<String>(), Events.ensureListOfStrings(new ArrayList<Object>()));
    }

    @Test(expected = ClassCastException.class)
    public void ensureListOfStringsRejectsNonStringValues() {
        Events.ensureListOfStrings(Arrays.<Object>asList("first", Integer.valueOf(2)));
    }

    @Test
    public void ensureListOfOrderedMapOfStringsToObjectsPreservesMaps() {
        Map<String, Object> first = new LinkedHashMap<>();
        first.put("one", Integer.valueOf(1));
        first.put("two", "value");
        List<Map<String, Object>> input = Arrays.asList(first);

        List<Map<String, Object>> result = Events.ensureListOfOrderedMapOfStringsToObjects(input);

        assertEquals(input, result);
        assertEquals(Arrays.asList("one", "two"), new ArrayList<String>(result.get(0).keySet()));
    }

    @Test
    public void ensureListOfOrderedMapOfStringsToObjectsReturnsEmptyListForEmptyInput() {
        assertEquals(new ArrayList<Map<String, Object>>(),
                Events.ensureListOfOrderedMapOfStringsToObjects(new ArrayList<Object>()));
    }

    @Test(expected = ClassCastException.class)
    public void ensureListOfOrderedMapOfStringsToObjectsRejectsNonMapValues() {
        Events.ensureListOfOrderedMapOfStringsToObjects(Arrays.<Object>asList("not a map"));
    }

    @Test
    public void ensureOrderedMapOfStringsToObjectsPreservesInsertionOrder() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("first", Integer.valueOf(1));
        input.put("second", "value");

        Map<String, Object> result = Events.ensureOrderedMapOfStringsToObjects(input);

        assertEquals(input, result);
        assertEquals(Arrays.asList("first", "second"), new ArrayList<String>(result.keySet()));
    }

    @Test(expected = ClassCastException.class)
    public void ensureOrderedMapOfStringsToObjectsRejectsNonStringKeys() {
        Map<Object, Object> input = new LinkedHashMap<>();
        input.put(Integer.valueOf(1), "value");
        Events.ensureOrderedMapOfStringsToObjects(input);
    }

    @Test
    public void ensureOrderedMapOfStringsToStringsPreservesInsertionOrder() {
        Map<String, String> input = new LinkedHashMap<>();
        input.put("first", "one");
        input.put("second", "two");

        Map<String, String> result = Events.ensureOrderedMapOfStringsToStrings(input);

        assertEquals(input, result);
        assertEquals(Arrays.asList("first", "second"), new ArrayList<String>(result.keySet()));
    }

    @Test(expected = ClassCastException.class)
    public void ensureOrderedMapOfStringsToStringsRejectsNonStringValues() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("key", Integer.valueOf(1));
        Events.ensureOrderedMapOfStringsToStrings(input);
    }
}
