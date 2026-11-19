package org.fuin.esc.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.json.Json;
import jakarta.json.JsonStructure;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.fuin.esc.api.ToJsonCapable;
import org.junit.Test;

public class DataWrapper_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        DataWrapper wrapper = new DataWrapper();
        assertEquals(null, wrapper.getObj());
    }

    @Test
    public void testConstructorWithObject() {
        Object obj = new Object();
        DataWrapper wrapper = new DataWrapper(obj);
        assertSame(obj, wrapper.getObj());
    }

    @Test
    public void testConstructorWithNull() {
        try {
            new DataWrapper(null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testGetObj() {
        Object obj = new Object();
        DataWrapper wrapper = new DataWrapper(obj);
        assertSame(obj, wrapper.getObj());
    }

    @Test
    public void testToJsonWithToJsonCapable() {
        TestToJsonCapable obj = new TestToJsonCapable();
        DataWrapper wrapper = new DataWrapper(obj);
        JsonStructure result = wrapper.toJson();
        assertSame(obj.getJson(), result);
    }

    @Test
    public void testToJsonWithJsonStructure() {
        JsonStructure json = Json.createObjectBuilder().add("test", "value").build();
        DataWrapper wrapper = new DataWrapper(json);
        JsonStructure result = wrapper.toJson();
        assertSame(json, result);
    }

    @Test
    public void testToJsonWithOtherObject() {
        Object obj = new Object();
        DataWrapper wrapper = new DataWrapper(obj);
        try {
            wrapper.toJson();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Wrapped object is not an instance of"));
            assertTrue(e.getMessage().contains("ToJsonCapable"));
            assertTrue(e.getMessage().contains("JsonStructure"));
        }
    }

    @Test
    public void testXmlRootElementAnnotation() {
        XmlRootElement annotation = DataWrapper.class.getAnnotation(XmlRootElement.class);
        assertEquals("Wrapper", annotation.name());
    }

    private static class TestToJsonCapable implements ToJsonCapable {
        private JsonStructure json = Json.createObjectBuilder().add("test", "value").build();

        @Override
        public JsonStructure toJson() {
            return json;
        }
    }

}
