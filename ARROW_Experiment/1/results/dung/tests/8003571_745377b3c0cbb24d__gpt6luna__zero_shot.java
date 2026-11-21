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
	}

	@Test
	public void constructorPreservesData() {
		Int32 message = new Int32(-42);

		assertEquals(-42, message.getData());
	}

	@Test
	public void clonePreservesDataAndCreatesAnotherInstance() {
		Int32 message = new Int32(17);

		Int32 clone = message.clone();

		assertEquals(17, clone.getData());
		assertNotSame(message, clone);
	}

	@Test
	public void fromJsonStringReadsData() {
		Int32 message = Int32.fromJsonString("{\"data\":23}");

		assertEquals(23, message.getData());
	}

	@Test
	public void fromMessageReadsData() {
		Int32 message = Int32.fromMessage(new Message("{\"data\":-8}"));

		assertEquals(-8, message.getData());
	}

	@Test
	public void fromJsonObjectReadsData() {
		JsonObject jsonObject = Json.createObjectBuilder().add("data", 31).build();

		Int32 message = Int32.fromJsonObject(jsonObject);

		assertEquals(31, message.getData());
	}

	@Test
	public void fromJsonObjectDefaultsMissingDataToZero() {
		JsonObject jsonObject = Json.createObjectBuilder().build();

		Int32 message = Int32.fromJsonObject(jsonObject);

		assertEquals(0, message.getData());
	}
}
