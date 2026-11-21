package com.codenotfound.kafka.serializer;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.Test;

public class AvroDeserializer_ArrowTest {

  @Test
  public void deserializeNullDataReturnsNull() {
    AvroDeserializer<org.apache.avro.specific.SpecificRecordBase> deserializer =
        new AvroDeserializer<>(org.apache.avro.specific.SpecificRecordBase.class);
    assertNull(deserializer.deserialize("topic", null));
  }

  @Test
  public void configureAndCloseDoNotThrow() {
    AvroDeserializer<org.apache.avro.specific.SpecificRecordBase> deserializer =
        new AvroDeserializer<>(org.apache.avro.specific.SpecificRecordBase.class);
    deserializer.configure(Collections.<String, Object>emptyMap(), false);
    deserializer.close();
  }

  @Test
  public void deserializeInvalidDataThrowsSerializationException() {
    AvroDeserializer<org.apache.avro.specific.SpecificRecordBase> deserializer =
        new AvroDeserializer<>(org.apache.avro.specific.SpecificRecordBase.class);
    try {
      deserializer.deserialize("topic", new byte[] {1});
      fail("Expected SerializationException");
    } catch (SerializationException exception) {
      assertNotNull(exception.getCause());
      assertTrue(exception.getMessage().contains("topic"));
    }
  }
}
