package com.codenotfound.kafka.serializer;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.Test;

public class AvroDeserializer_ArrowTest {

    @Test
    public void testDeserializeWithNullData() {
        AvroDeserializer<SpecificRecordBase> deserializer = new AvroDeserializer<>(SpecificRecordBase.class);
        SpecificRecordBase result = deserializer.deserialize("test-topic", null);
        assertNull(result);
    }

    @Test
    public void testDeserializeWithEmptyData() {
        AvroDeserializer<SpecificRecordBase> deserializer = new AvroDeserializer<>(SpecificRecordBase.class);
        SpecificRecordBase result = deserializer.deserialize("test-topic", new byte[0]);
        assertNull(result);
    }

    @Test
    public void testConfigure() {
        AvroDeserializer<SpecificRecordBase> deserializer = new AvroDeserializer<>(SpecificRecordBase.class);
        Map<String, Object> configs = new HashMap<>();
        deserializer.configure(configs, false);
        // Should not throw any exception
    }

    @Test
    public void testClose() {
        AvroDeserializer<SpecificRecordBase> deserializer = new AvroDeserializer<>(SpecificRecordBase.class);
        deserializer.close();
        // Should not throw any exception
    }

    @Test(expected = SerializationException.class)
    public void testDeserializeWithInvalidData() throws Exception {
        AvroDeserializer<SpecificRecordBase> deserializer = new AvroDeserializer<>(SpecificRecordBase.class);
        byte[] invalidData = "invalid".getBytes();
        deserializer.deserialize("test-topic", invalidData);
    }
}
