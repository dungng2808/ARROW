package com.codenotfound.kafka.serializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.junit.Test;
import org.springframework.kafka.test.rule.KafkaEmbedded;

public class AvroDeserializer_ArrowTest {

  @Test
  public void testConstructor() {
    Class<SpecificRecordBase> targetType = SpecificRecordBase.class;
    AvroDeserializer<SpecificRecordBase> deserializer = new AvroDeserializer<>(targetType);
    assertThat(deserializer).isNotNull();
  }

  @Test
  public void testConfigure() {
    Class<SpecificRecordBase> targetType = SpecificRecordBase.class;
    AvroDeserializer<SpecificRecordBase> deserializer = new AvroDeserializer<>(targetType);
    Map<String, Object> configs = new HashMap<>();
    deserializer.configure(configs, false);
    // Should not throw any exception
  }

  @Test
  public void testClose() {
    Class<SpecificRecordBase> targetType = SpecificRecordBase.class;
    AvroDeserializer<SpecificRecordBase> deserializer = new AvroDeserializer<>(targetType);
    deserializer.close();
    // Should not throw any exception
  }

  @Test
  public void testDeserializeNullData() {
    Class<SpecificRecordBase> targetType = SpecificRecordBase.class;
    AvroDeserializer<SpecificRecordBase> deserializer = new AvroDeserializer<>(targetType);
    String topic = "test-topic";
    byte[] data = null;
    SpecificRecordBase result = deserializer.deserialize(topic, data);
    assertThat(result).isNull();
  }

  @Test
  public void testDeserializeEmptyData() {
    Class<SpecificRecordBase> targetType = SpecificRecordBase.class;
    AvroDeserializer<SpecificRecordBase> deserializer = new AvroDeserializer<>(targetType);
    String topic = "test-topic";
    byte[] data = new byte[0];
    SpecificRecordBase result = deserializer.deserialize(topic, data);
    // Should not throw exception but return null due to empty data
    assertThat(result).isNull();
  }

  @Test(expected = org.apache.kafka.common.errors.SerializationException.class)
  public void testDeserializeInvalidData() {
    Class<SpecificRecordBase> targetType = SpecificRecordBase.class;
    AvroDeserializer<SpecificRecordBase> deserializer = new AvroDeserializer<>(targetType);
    String topic = "test-topic";
    byte[] data = {1, 2, 3, 4, 5}; // Invalid Avro data
    deserializer.deserialize(topic, data);
    // Should throw SerializationException
  }
}
