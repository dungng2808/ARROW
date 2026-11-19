package com.amazonaws.services.lambda.runtime.events.transformers.dynamodb;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import org.junit.Test;
import software.amazon.awssdk.services.dynamodb.model.StreamRecord.Builder;
import software.amazon.awssdk.services.dynamodb.model.StreamViewType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueMap;
import software.amazon.awssdk.services.dynamodb.model.StreamRecord;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class DynamodbStreamRecordTransformer_ArrowTest {

    @Test
    public void testToStreamRecordV2() {
        // Create a mock StreamRecord with all fields populated
        StreamRecord streamRecord = new StreamRecord();
        streamRecord.setApproximateCreationDateTime(java.util.Date.from(Instant.now()));
        streamRecord.setKeys(createAttributeMap());
        streamRecord.setNewImage(createAttributeMap());
        streamRecord.setOldImage(createAttributeMap());
        streamRecord.setSequenceNumber("12345");
        streamRecord.setSizeBytes(100L);
        streamRecord.setStreamViewType(StreamViewType.NEW_AND_OLD_IMAGES);

        // Transform the record
        StreamRecord result = DynamodbStreamRecordTransformer.toStreamRecordV2(streamRecord);

        // Verify the transformation
        assertNotNull(result);
        assertEquals(streamRecord.getSequenceNumber(), result.sequenceNumber());
        assertEquals(streamRecord.getSizeBytes(), result.sizeBytes());
        assertEquals(streamRecord.getStreamViewType(), result.streamViewType());
        assertEquals(streamRecord.getKeys().size(), result.keys().size());
        assertEquals(streamRecord.getNewImage().size(), result.newImage().size());
        assertEquals(streamRecord.getOldImage().size(), result.oldImage().size());
    }

    @Test
    public void testToStreamRecordV2WithNullImages() {
        // Create a mock StreamRecord with null images
        StreamRecord streamRecord = new StreamRecord();
        streamRecord.setApproximateCreationDateTime(java.util.Date.from(Instant.now()));
        streamRecord.setKeys(createAttributeMap());
        streamRecord.setNewImage(null);
        streamRecord.setOldImage(null);
        streamRecord.setSequenceNumber("12345");
        streamRecord.setSizeBytes(100L);
        streamRecord.setStreamViewType(StreamViewType.NEW_AND_OLD_IMAGES);

        // Transform the record
        StreamRecord result = DynamodbStreamRecordTransformer.toStreamRecordV2(streamRecord);

        // Verify the transformation
        assertNotNull(result);
        assertEquals(streamRecord.getSequenceNumber(), result.sequenceNumber());
        assertEquals(streamRecord.getSizeBytes(), result.sizeBytes());
        assertEquals(streamRecord.getStreamViewType(), result.streamViewType());
        assertEquals(streamRecord.getKeys().size(), result.keys().size());
        assertNull(result.newImage());
        assertNull(result.oldImage());
    }

    private AttributeValueMap createAttributeMap() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("key1", AttributeValue.builder().s("value1").build());
        map.put("key2", AttributeValue.builder().n("123").build());
        return AttributeValueMap.builder().putAll(map).build();
    }
}
