package com.amazonaws.services.lambda.runtime.events.transformers.dynamodb;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.StreamRecord.Builder;
import software.amazon.awssdk.services.dynamodb.model.StreamViewType;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class DynamodbStreamRecordTransformer_ArrowTest {

    @Test
    public void testToStreamRecordV2_NullNewImageAndOldImage() {
        // Given
        StreamRecord streamRecord = new StreamRecord();
        streamRecord.setApproximateCreationDateTime(Instant.now());
        streamRecord.setKeys(new HashMap<>());
        streamRecord.setSequenceNumber("123");
        streamRecord.setSizeBytes(100L);
        streamRecord.setStreamViewType(StreamViewType.NEW_AND_OLD_IMAGES);

        // When
        software.amazon.awssdk.services.dynamodb.model.StreamRecord result =
            DynamodbStreamRecordTransformer.toStreamRecordV2(streamRecord);

        // Then
        assertNotNull(result);
        assertEquals(streamRecord.getApproximateCreationDateTime().toInstant(), result.approximateCreationDateTime());
        assertEquals(DynamodbAttributeValueTransformer.toAttributeValueMapV2(streamRecord.getKeys()), result.keys());
        assertNull(result.newImage());
        assertNull(result.oldImage());
        assertEquals(streamRecord.getSequenceNumber(), result.sequenceNumber());
        assertEquals(streamRecord.getSizeBytes(), result.sizeBytes());
        assertEquals(streamRecord.getStreamViewType(), result.streamViewType());
    }

    @Test
    public void testToStreamRecordV2_WithNewImageAndOldImage() {
        // Given
        Map<String, Object> keys = new HashMap<>();
        keys.put("id", "test-id");

        Map<String, Object> newImage = new HashMap<>();
        newImage.put("name", "test-name");

        Map<String, Object> oldImage = new HashMap<>();
        oldImage.put("name", "old-name");

        StreamRecord streamRecord = new StreamRecord();
        streamRecord.setApproximateCreationDateTime(Instant.now());
        streamRecord.setKeys(keys);
        streamRecord.setNewImage(newImage);
        streamRecord.setOldImage(oldImage);
        streamRecord.setSequenceNumber("456");
        streamRecord.setSizeBytes(200L);
        streamRecord.setStreamViewType(StreamViewType.NEW_IMAGE_ONLY);

        // When
        software.amazon.awssdk.services.dynamodb.model.StreamRecord result =
            DynamodbStreamRecordTransformer.toStreamRecordV2(streamRecord);

        // Then
        assertNotNull(result);
        assertEquals(streamRecord.getApproximateCreationDateTime().toInstant(), result.approximateCreationDateTime());
        assertEquals(DynamodbAttributeValueTransformer.toAttributeValueMapV2(streamRecord.getKeys()), result.keys());
        assertEquals(DynamodbAttributeValueTransformer.toAttributeValueMapV2(streamRecord.getNewImage()), result.newImage());
        assertEquals(DynamodbAttributeValueTransformer.toAttributeValueMapV2(streamRecord.getOldImage()), result.oldImage());
        assertEquals(streamRecord.getSequenceNumber(), result.sequenceNumber());
        assertEquals(streamRecord.getSizeBytes(), result.sizeBytes());
        assertEquals(streamRecord.getStreamViewType(), result.streamViewType());
    }

    @Test
    public void testToStreamRecordV2_EmptyMaps() {
        // Given
        StreamRecord streamRecord = new StreamRecord();
        streamRecord.setApproximateCreationDateTime(Instant.now());
        streamRecord.setKeys(new HashMap<>());
        streamRecord.setNewImage(new HashMap<>());
        streamRecord.setOldImage(new HashMap<>());
        streamRecord.setSequenceNumber("");
        streamRecord.setSizeBytes(0L);
        streamRecord.setStreamViewType(StreamViewType.KEYS_ONLY);

        // When
        software.amazon.awssdk.services.dynamodb.model.StreamRecord result =
            DynamodbStreamRecordTransformer.toStreamRecordV2(streamRecord);

        // Then
        assertNotNull(result);
        assertEquals(streamRecord.getApproximateCreationDateTime().toInstant(), result.approximateCreationDateTime());
        assertEquals(DynamodbAttributeValueTransformer.toAttributeValueMapV2(streamRecord.getKeys()), result.keys());
        assertEquals(DynamodbAttributeValueTransformer.toAttributeValueMapV2(streamRecord.getNewImage()), result.newImage());
        assertEquals(DynamodbAttributeValueTransformer.toAttributeValueMapV2(streamRecord.getOldImage()), result.oldImage());
        assertEquals(streamRecord.getSequenceNumber(), result.sequenceNumber());
        assertEquals(streamRecord.getSizeBytes(), result.sizeBytes());
        assertEquals(streamRecord.getStreamViewType(), result.streamViewType());
    }
}
