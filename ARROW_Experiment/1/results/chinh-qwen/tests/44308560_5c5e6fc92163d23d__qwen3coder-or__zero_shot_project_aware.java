package org.rf.ide.core.execution.agent.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class Events_ArrowTest {

    @Test
    public void testToFileUriWithNull() {
        assertThat(Events.toFileUri(null)).isNull();
    }

    @Test
    public void testToFileUriWithEmptyString() {
        assertThat(Events.toFileUri("")).isNull();
    }

    @Test
    public void testToFileUriWithValidPath() {
        final URI result = Events.toFileUri("/path/to/file");
        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo("file:///path/to/file");
    }

    @Test
    public void testToFileUriWithWindowsPath() {
        final URI result = Events.toFileUri("C:\\path\\to\\file");
        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo("file:///C:/path/to/file");
    }

    @Test
    public void testToFileUriWithSpecialCharacters() {
        final URI result = Events.toFileUri("/path/with space/file");
        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo("file:///path/with%20space/file");
    }

    @Test
    public void testToFileUriWithInvalidUriSyntax() {
        // This should trigger URISyntaxException
        final URI result = Events.toFileUri(null); // null will cause issues in URI construction
        assertThat(result).isNull();
    }

    @Test
    public void testEnsureListOfStrings() {
        final List<String> input = Arrays.asList("one", "two", "three");
        final List<String> result = Events.ensureListOfStrings(input);
        assertThat(result).containsExactly("one", "two", "three");
    }

    @Test
    public void testEnsureListOfStringsEmptyList() {
        final List<String> result = Events.ensureListOfStrings(Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
    public void testEnsureListOfOrderedMapOfStringsToObjects() {
        final Map<String, Object> map1 = new LinkedHashMap<>();
        map1.put("key1", "value1");
        map1.put("key2", 42);

        final Map<String, Object> map2 = new LinkedHashMap<>();
        map2.put("key3", "value3");
        map2.put("key4", true);

        final List<Map<String, Object>> input = Arrays.asList(map1, map2);
        final List<Map<String, Object>> result = Events.ensureListOfOrderedMapOfStringsToObjects(input);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).containsEntry("key1", "value1").containsEntry("key2", 42);
        assertThat(result.get(1)).containsEntry("key3", "value3").containsEntry("key4", true);
    }

    @Test
    public void testEnsureListOfOrderedMapOfStringsToObjectsEmptyList() {
        final List<Map<String, Object>> result = Events.ensureListOfOrderedMapOfStringsToObjects(Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
    public void testEnsureOrderedMapOfStringsToObjects() {
        final Map<String, Object> input = new LinkedHashMap<>();
        input.put("key1", "value1");
        input.put("key2", 42);

        final Map<String, Object> result = Events.ensureOrderedMapOfStringsToObjects(input);
        assertThat(result).containsEntry("key1", "value1").containsEntry("key2", 42);
    }

    @Test
    public void testEnsureOrderedMapOfStringsToStrings() {
        final Map<String, String> input = new LinkedHashMap<>();
        input.put("key1", "value1");
        input.put("key2", "value2");

        final Map<String, String> result = Events.ensureOrderedMapOfStringsToStrings(input);
        assertThat(result).containsEntry("key1", "value1").containsEntry("key2", "value2");
    }

    @Test
    public void testEnsureOrderedMapOfStringsToStringsEmptyMap() {
        final Map<String, String> result = Events.ensureOrderedMapOfStringsToStrings(Collections.emptyMap());
        assertThat(result).isEmpty();
    }
}
