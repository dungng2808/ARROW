package org.fuin.esc.spi;

import jakarta.json.JsonStructure;
import org.fuin.esc.api.ToJsonCapable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DataWrapper_ArrowTest {

    @Test
    public void testConstructor() {
        final String testData = "test";
        final DataWrapper wrapper = new DataWrapper(testData);
        assertThat(wrapper.getObj()).isSameAs(testData);
    }

    @Test
    public void testGetObj() {
        final Integer testData = 42;
        final DataWrapper wrapper = new DataWrapper(testData);
        assertThat(wrapper.getObj()).isSameAs(testData);
    }

    @Test
    public void testToJsonWithToJsonCapable() {
        final TestToJsonCapable testData = new TestToJsonCapable();
        final DataWrapper wrapper = new DataWrapper(testData);
        final JsonStructure result = wrapper.toJson();
        assertThat(result).isSameAs(testData.getJsonStructure());
    }

    @Test
    public void testToJsonWithJsonStructure() {
        final TestJsonStructure testData = new TestJsonStructure();
        final DataWrapper wrapper = new DataWrapper(testData);
        final JsonStructure result = wrapper.toJson();
        assertThat(result).isSameAs(testData);
    }

    @Test
    public void testToJsonWithOtherObject() {
        final String testData = "test";
        final DataWrapper wrapper = new DataWrapper(testData);
        assertThatThrownBy(() -> wrapper.toJson())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Wrapped object is not an instance of 'ToJsonCapable' or 'JsonStructure'");
    }

    private static class TestToJsonCapable implements ToJsonCapable {

        private final JsonStructure jsonStructure = new TestJsonStructure();

        @Override
        public JsonStructure toJson() {
            return jsonStructure;
        }

        public JsonStructure getJsonStructure() {
            return jsonStructure;
        }

    }

    private static class TestJsonStructure implements JsonStructure {

        // Empty implementation for testing purposes

    }

}
