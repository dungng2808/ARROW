package org.fuin.esc.spi;

import jakarta.json.JsonStructure;
import org.fuin.esc.api.ToJsonCapable;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataWrapper_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        DataWrapper wrapper = new DataWrapper();
        assertNull(wrapper.getObj());
    }

    @Test
    public void testConstructorWithObject() {
        Object testObj = new Object();
        DataWrapper wrapper = new DataWrapper(testObj);
        assertEquals(testObj, wrapper.getObj());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNull() {
        new DataWrapper(null);
    }

    @Test
    public void testGetObj() {
        Object testObj = "test";
        DataWrapper wrapper = new DataWrapper(testObj);
        assertEquals(testObj, wrapper.getObj());
    }

    @Test
    public void testToJsonWithToJsonCapable() {
        TestToJsonCapable testObj = new TestToJsonCapable();
        DataWrapper wrapper = new DataWrapper(testObj);
        JsonStructure result = wrapper.toJson();
        assertEquals(testObj.getJson(), result);
    }

    @Test
    public void testToJsonWithJsonStructure() {
        TestJsonStructure testObj = new TestJsonStructure();
        DataWrapper wrapper = new DataWrapper(testObj);
        JsonStructure result = wrapper.toJson();
        assertEquals(testObj, result);
    }

    @Test(expected = IllegalStateException.class)
    public void testToJsonWithOtherObject() {
        Object testObj = new Object();
        DataWrapper wrapper = new DataWrapper(testObj);
        wrapper.toJson();
    }

    private static class TestToJsonCapable implements ToJsonCapable {
        private JsonStructure json = new TestJsonStructure();

        @Override
        public JsonStructure toJson() {
            return json;
        }

        public JsonStructure getJson() {
            return json;
        }
    }

    private static class TestJsonStructure implements JsonStructure {
        // Simple implementation for testing
    }

}
