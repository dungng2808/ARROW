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
        ToJsonCapable mockToJson = new ToJsonCapable() {
            @Override
            public JsonStructure toJson() {
                return new JsonStructure() {};
            }
        };
        
        DataWrapper wrapper = new DataWrapper(mockToJson);
        JsonStructure result = wrapper.toJson();
        assertNotNull(result);
    }

    @Test
    public void testToJsonWithJsonStructure() {
        JsonStructure mockJsonStructure = new JsonStructure() {};
        DataWrapper wrapper = new DataWrapper(mockJsonStructure);
        JsonStructure result = wrapper.toJson();
        assertEquals(mockJsonStructure, result);
    }

    @Test(expected = IllegalStateException.class)
    public void testToJsonWithNonToJsonCapableAndNonJsonStructure() {
        Object testObj = new Object();
        DataWrapper wrapper = new DataWrapper(testObj);
        wrapper.toJson();
    }

}
