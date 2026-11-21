package edu.wpi.rail.jrosbridge.messages.std;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.json.JsonObject;

import org.junit.Test;

public class Int32_ArrowTest {

	@Test
	public void testDefaultConstructor() {
		Int32 int32 = new Int32();
		assertEquals(0, int32.getData());
		assertNotNull(int32.toJsonObject());
	}

	@Test
	public void testConstructorWithData() {
		Int32 int32 = new Int32(42);
		assertEquals(42, int32.getData());
		assertNotNull(int32.toJsonObject());
	}

	@Test
	public void testGetData() {
		Int32 int32 = new Int32(-123);
		assertEquals(-123, int32.getData());
	}

	@Test
	public void testClone() {
		Int32 original = new Int32(999);
		Int32 cloned = original.clone();
		assertEquals(original.getData(), cloned.getData());
		assertEquals(original.getType(), cloned.getType());
	}

	@Test
	public void testFromJsonString() {
		String jsonString = "{\"data\": 12345}";
		Int32 int32 = Int32.fromJsonString(jsonString);
		assertEquals(12345, int32.getData());
	}

	@Test
	public void testFromMessage() {
		Message message = new Message("{\"data\": 67890}");
		Int32 int32 = Int32.fromMessage(message);
		assertEquals(67890, int32.getData());
	}

	@Test
	public void testFromJsonObject() {
		JsonObject jsonObject = Json.createObjectBuilder().add("data", 54321).build();
		Int32 int32 = Int32.fromJsonObject(jsonObject);
		assertEquals(54321, int32.getData());
	}

	@Test
	public void testFromJsonObjectMissingField() {
		JsonObject jsonObject = Json.createObjectBuilder().build();
		Int32 int32 = Int32.fromJsonObject(jsonObject);
		assertEquals(0, int32.getData());
	}

	@Test
	public void testGetType() {
		Int32 int32 = new Int32(1);
		assertEquals("std_msgs/Int32", int32.getType());
	}
}
