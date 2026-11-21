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
    public void constructorStoresWrappedObject() {
        Object value = new Object();

        DataWrapper wrapper = new DataWrapper(value);

        assertSame(value, wrapper.getObj());
    }

    @Test
    public void toJsonReturnsWrappedJsonStructure() {
        JsonObject json = Json.createObjectBuilder().add("value", "test").build();
        DataWrapper wrapper = new DataWrapper(json);

        assertSame(json, wrapper.toJson());
    }

    @Test
    public void toJsonDelegatesToWrappedToJsonCapable() {
        final JsonObject json = Json.createObjectBuilder().add("value", "test").build();
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
    public void toJsonRejectsUnsupportedWrappedObject() {
        DataWrapper wrapper = new DataWrapper("unsupported");

        try {
            wrapper.toJson();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            assertEquals("Wrapped object is not an instance of 'ToJsonCapable' or 'JsonStructure': unsupported [java.lang.String]",
                    ex.getMessage());
        }
    }

    @Test(expected = RuntimeException.class)
    public void constructorRejectsNull() {
        new DataWrapper(null);
    }
}
