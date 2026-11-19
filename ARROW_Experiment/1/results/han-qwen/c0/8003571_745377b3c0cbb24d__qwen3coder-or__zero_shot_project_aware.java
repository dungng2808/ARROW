package edu.wpi.rail.jrosbridge.messages.std;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

public class Int32_ArrowTest {

	@Test
	public void testConstructorDefault() {
		Int32 int32 = new Int32();
		assertEquals(0, int32.getData());
		assertNotNull(int32.toJsonObject());
		assertEquals("std_msgs/Int32", int32.getType());
	}

	@Test
	public void testConstructorWithData() {
		Int32 int32 = new Int32(42);
		assertEquals(42, int32.getData());
		assertNotNull(int32.toJsonObject());
		assertEquals("std_msgs/Int32", int32.getType());
	}

	@Test
	public void testGetData() {
		Int32 int32 = new Int32(-100);
		assertEquals(-100, int32.getData());
	}

	@Test
	public void testClone() {
		Int32 original = new Int32(123);
		Int32 cloned = original.clone();
		
		assertEquals(original.getData(), cloned.getData());
		assertEquals(original.getType(), cloned.getType());
		assertTrue(original != cloned);
	}

	@Test
	public void testFromJsonString() {
		String jsonString = "{\"data\": 50}";
		Int32 int32 = Int32.fromJsonString(jsonString);
		assertEquals(50, int32.getData());
	}

	@Test
	public void testFromMessage() {
		JsonObject jsonObject = Json.createObjectBuilder()
				.add("data", 75)
				.build();
		Message message = new Message(jsonObject);
		Int32 int32 = Int32.fromMessage(message);
		assertEquals(75, int32.getData());
	}

	@Test
	public void testFromJsonObject() {
		JsonObject jsonObject = Json.createObjectBuilder()
				.add("data", -25)
				.build();
		Int32 int32 = Int32.fromJsonObject(jsonObject);
		assertEquals(-25, int32.getData());
	}

	@Test
	public void testFromJsonObjectMissingField() {
		JsonObject jsonObject = Json.createObjectBuilder()
				.add("other_field", "value")
				.build();
		Int32 int32 = Int32.fromJsonObject(jsonObject);
		assertEquals(0, int32.getData());
	}
}
