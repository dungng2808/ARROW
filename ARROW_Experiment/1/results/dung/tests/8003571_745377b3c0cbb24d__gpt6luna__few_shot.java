package edu.wpi.rail.jrosbridge.messages.std;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

import edu.wpi.rail.jrosbridge.messages.Message;

public class Int32_ArrowTest {

    @Test
    public void defaultConstructorUsesZero() {
        Int32 message = new Int32();

        assertEquals(0, message.getData());
        assertEquals("{\"data\":0}", message.toString());
    }

    @Test
    public void constructorStoresPositiveData() {
        Int32 message = new Int32(42);

        assertEquals(42, message.getData());
        assertEquals("{\"data\":42}", message.toString());
    }

    @Test
    public void constructorStoresNegativeData() {
        Int32 message = new Int32(-7);

        assertEquals(-7, message.getData());
        assertEquals("{\"data\":-7}", message.toString());
    }

    @Test
    public void cloneCreatesEquivalentDistinctMessage() {
        Int32 original = new Int32(15);
        Int32 clone = original.clone();

        assertEquals(15, clone.getData());
        assertNotSame(original, clone);
    }

    @Test
    public void fromJsonStringReadsData() {
        Int32 message = Int32.fromJsonString("{\"data\":23}");

        assertEquals(23, message.getData());
    }

    @Test
    public void fromMessageReadsData() {
        Message source = new Message("{\"data\":-12}");
        Int32 message = Int32.fromMessage(source);

        assertEquals(-12, message.getData());
    }

    @Test
    public void fromJsonObjectReadsData() {
        JsonObject object = Json.createObjectBuilder().add("data", 31).build();
        Int32 message = Int32.fromJsonObject(object);

        assertEquals(31, message.getData());
    }

    @Test
    public void fromJsonObjectDefaultsMissingDataToZero() {
        JsonObject object = Json.createObjectBuilder().build();
        Int32 message = Int32.fromJsonObject(object);

        assertEquals(0, message.getData());
    }
}
