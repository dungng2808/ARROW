package com.codenotfound.kafka.serializer;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvroDeserializer_ArrowTest {

  public static class TestRecord extends SpecificRecordBase {
    private static final Schema SCHEMA =
        new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":[{\"name\":\"value\",\"type\":\"string\"}]}");

    private String value;

    public TestRecord() {
    }

    @Override
    public Schema getSchema() {
      return SCHEMA;
    }

    @Override
    public Object get(int field) {
      if (field == 0) {
        return value;
      }
      throw new IndexOutOfBoundsException();
    }

    @Override
    public void put(int field, Object value) {
      if (field == 0) {
        this.value = (String) value;
        return;
      }
      throw new IndexOutOfBoundsException();
    }
  }

  @Test
  public void deserializeNullDataReturnsNull() {
    AvroDeserializer<TestRecord> deserializer = new AvroDeserializer<>(TestRecord.class);
    assertNull(deserializer.deserialize("topic", null));
  }

  @Test
  public void deserializeValidAvroDataReturnsRecord() throws IOException {
    Schema schema =
        new Schema.Parser().parse(
            "{\"type\":\"record\",\"name\":\"TestRecord\",\"fields\":[{\"name\":\"value\",\"type\":\"string\"}]}");
    GenericRecord record = new GenericData.Record(schema);
    record.put("value", "hello");

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(output, null);
    new org.apache.avro.generic.GenericDatumWriter<GenericRecord>(schema).write(record, encoder);
    encoder.flush();

    AvroDeserializer<TestRecord> deserializer = new AvroDeserializer<>(TestRecord.class);
    TestRecord result = deserializer.deserialize("topic", output.toByteArray());

    org.junit.Assert.assertEquals("hello", result.get(0));
  }

  @Test
  public void deserializeInvalidDataThrowsSerializationException() {
    AvroDeserializer<TestRecord> deserializer = new AvroDeserializer<>(TestRecord.class);
    try {
      deserializer.deserialize("topic", new byte[] {1});
      fail("Expected SerializationException");
    } catch (SerializationException expected) {
      org.junit.Assert.assertTrue(expected.getMessage().contains("topic"));
    }
  }

  @Test
  public void configureAndCloseComplete() {
    AvroDeserializer<TestRecord> deserializer = new AvroDeserializer<>(TestRecord.class);
    deserializer.configure(Collections.<String, Object>emptyMap(), false);
    deserializer.close();
    assertNull(deserializer.deserialize("topic", null));
  }
}
