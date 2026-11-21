package edu.wpi.rail.jrosbridge.messages.std;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class Int32_ArrowTest {

    @Test
    public void defaultConstructorUsesZero() {
        Int32 message = new Int32();

        assertEquals(0, message.getData());
    }

    @Test
    public void constructorStoresData() {
        Int32 message = new Int32(-17);

        assertEquals(-17, message.getData());
    }

    @Test
    public void cloneCreatesEquivalentDistinctMessage() {
        Int32 original = new Int32(42);

        Int32 clone = original.clone();

        assertEquals(42, clone.getData());
        assertNotSame(original, clone);
    }

    @Test
    public void fromJsonStringReadsData() {
        Int32 message = Int32.fromJsonString("{\"data\":23}");

        assertEquals(23, message.getData());
    }

    @Test
    public void fromMessageReadsData() {
        Int32 message = Int32.fromMessage(new Int32(31));

        assertEquals(31, message.getData());
    }

    @Test
    public void fromJsonObjectReadsData() {
        JsonObject jsonObject = Json.createObjectBuilder().add("data", -8).build();

        Int32 message = Int32.fromJsonObject(jsonObject);

        assertEquals(-8, message.getData());
    }

    @Test
    public void fromJsonObjectDefaultsMissingDataToZero() {
        JsonObject jsonObject = Json.createObjectBuilder().build();

        Int32 message = Int32.fromJsonObject(jsonObject);

        assertEquals(0, message.getData());
    }
}
