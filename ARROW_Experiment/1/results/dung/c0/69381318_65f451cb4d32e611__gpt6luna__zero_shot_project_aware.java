package com.codenotfound.kafka.serializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.Test;

public class AvroDeserializer_ArrowTest {

  public static class TestRecord extends SpecificRecordBase {
    private static final Schema SCHEMA = new Schema.Parser().parse(
        "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":[{\"name\":\"value\",\"type\":\"int\"}]}");

    private int value;

    @Override
    public void put(int field, Object value) {
      if (field == 0) {
        this.value = (Integer) value;
      }
    }

    @Override
    public Object get(int field) {
      if (field == 0) {
        return value;
      }
      throw new IndexOutOfBoundsException();
    }

    @Override
    public Schema getSchema() {
      return SCHEMA;
    }
  }

  @Test
  public void deserializeReturnsDecodedRecord() {
    AvroDeserializer<TestRecord> deserializer = new AvroDeserializer<>(TestRecord.class);
    TestRecord result = deserializer.deserialize("topic", new byte[] {42});
    assertNotNull(result);
    assertEquals(21, result.get(0));
  }

  @Test
  public void deserializeReturnsNullForNullData() {
    AvroDeserializer<TestRecord> deserializer = new AvroDeserializer<>(TestRecord.class);
    assertNull(deserializer.deserialize("topic", null));
  }

  @Test
  public void deserializeWrapsInvalidDataInSerializationException() {
    AvroDeserializer<TestRecord> deserializer = new AvroDeserializer<>(TestRecord.class);
    try {
      deserializer.deserialize("topic", new byte[] {(byte) 0x80});
      fail("Expected SerializationException");
    } catch (SerializationException ex) {
      assertTrue(ex.getCause() != null);
      assertEquals("Can't deserialize data '[-128]' from topic 'topic'", ex.getMessage());
    }
  }

  @Test
  public void configureAndCloseDoNotThrow() {
    AvroDeserializer<TestRecord> deserializer = new AvroDeserializer<>(TestRecord.class);
    deserializer.configure(Collections.<String, Object>emptyMap(), false);
    deserializer.close();
    assertTrue(true);
  }
}
