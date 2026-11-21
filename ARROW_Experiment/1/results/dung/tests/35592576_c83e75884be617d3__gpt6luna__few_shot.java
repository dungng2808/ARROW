package org.fuin.esc.spi;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import org.fuin.esc.api.ToJsonCapable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class DataWrapper_ArrowTest {

    @Test
    public void wrapsAndReturnsObject() {
        Object value = new Object();
        DataWrapper wrapper = new DataWrapper(value);
        assertSame(value, wrapper.getObj());
    }

    @Test
    public void returnsWrappedJsonStructure() {
        JsonObject json = Json.createObjectBuilder().add("name", "value").build();
        DataWrapper wrapper = new DataWrapper(json);
        assertSame(json, wrapper.toJson());
    }

    @Test
    public void delegatesToJsonCapableObject() {
        final JsonObject json = Json.createObjectBuilder().add("name", "value").build();
        ToJsonCapable capable = new ToJsonCapable() {
            @Override
            public JsonStructure toJson() {
                return json;
            }
        };

        DataWrapper wrapper = new DataWrapper(capable);
        assertSame(json, wrapper.toJson());
    }

    @Test
    public void rejectsObjectThatCannotBeConvertedToJson() {
        DataWrapper wrapper = new DataWrapper("value");
        try {
            wrapper.toJson();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertEquals("Wrapped object is not an instance of 'ToJsonCapable' or 'JsonStructure': value [java.lang.String]",
                    ex.getMessage());
        }
    }

    @Test(expected = Exception.class)
    public void rejectsNullObject() {
        new DataWrapper(null);
    }
}
