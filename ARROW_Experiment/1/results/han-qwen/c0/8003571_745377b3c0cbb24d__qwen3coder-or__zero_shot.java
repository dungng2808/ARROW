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
	}

	@Test
	public void testConstructorWithData() {
		Int32 int32 = new Int32(42);
		assertEquals(42, int32.getData());
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
		assertTrue(original != cloned);
	}

	@Test
	public void testFromJsonString() {
		String jsonString = "{\"data\": 999}";
		Int32 int32 = Int32.fromJsonString(jsonString);
		assertEquals(999, int32.getData());
	}

	@Test
	public void testFromMessage() {
		Message message = new Message("{\"data\": 555}");
		Int32 int32 = Int32.fromMessage(message);
		assertEquals(555, int32.getData());
	}

	@Test
	public void testFromJsonObject() {
		JsonObject jsonObject = Json.createObjectBuilder()
				.add("data", 777)
				.build();
		Int32 int32 = Int32.fromJsonObject(jsonObject);
		assertEquals(777, int32.getData());
	}

	@Test
	public void testFromJsonObjectMissingField() {
		JsonObject jsonObject = Json.createObjectBuilder()
				.add("other_field", "value")
				.build();
		Int32 int32 = Int32.fromJsonObject(jsonObject);
		assertEquals(0, int32.getData());
	}

	@Test
	public void testMessageType() {
		Int32 int32 = new Int32();
		assertEquals("std_msgs/Int32", int32.getMessageType());
	}

	@Test
	public void testMessageDataField() {
		Int32 int32 = new Int32(1000);
		JsonObject jsonObject = int32.toJsonObject();
		assertNotNull(jsonObject);
		assertTrue(jsonObject.containsKey("data"));
		assertEquals(1000, jsonObject.getInt("data"));
	}
}
