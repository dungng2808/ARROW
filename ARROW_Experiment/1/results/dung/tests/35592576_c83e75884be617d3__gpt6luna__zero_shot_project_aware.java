package org.fuin.esc.spi;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import org.fuin.esc.api.ToJsonCapable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DataWrapper_ArrowTest {

    @Test
    public void getObjReturnsWrappedObject() {
        Object wrapped = new Object();

        DataWrapper wrapper = new DataWrapper(wrapped);

        assertSame(wrapped, wrapper.getObj());
    }

    @Test
    public void toJsonReturnsWrappedJsonStructure() {
        JsonObject json = Json.createObjectBuilder().add("value", "test").build();
        DataWrapper wrapper = new DataWrapper(json);

        assertSame(json, wrapper.toJson());
    }

    @Test
    public void toJsonDelegatesToWrappedToJsonCapable() {
        JsonObject json = Json.createObjectBuilder().add("value", "test").build();
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
        String wrapped = "unsupported";
        DataWrapper wrapper = new DataWrapper(wrapped);

        IllegalStateException exception = assertThrows(IllegalStateException.class, wrapper::toJson);

        assertEquals("Wrapped object is not an instance of 'ToJsonCapable' or 'JsonStructure': unsupported [java.lang.String]",
                exception.getMessage());
    }

    @Test
    public void constructorRejectsNull() {
        assertThrows(RuntimeException.class, () -> new DataWrapper(null));
    }
}
