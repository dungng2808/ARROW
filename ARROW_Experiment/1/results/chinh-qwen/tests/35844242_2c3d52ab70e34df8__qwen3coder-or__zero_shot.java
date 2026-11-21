package com.amazonaws.services.lambda.runtime.events.transformers.dynamodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import software.amazon.awssdk.services.dynamodb.model.StreamRecord.Builder;
import software.amazon.awssdk.services.dynamodb.model.StreamViewType;

public class DynamodbStreamRecordTransformer_ArrowTest {

    @Test
    public void testToStreamRecordV2_NullNewImageAndOldImage() {
        // Given
        StreamRecord streamRecord = mock(StreamRecord.class);
        when(streamRecord.getApproximateCreationDateTime()).thenReturn(new java.util.Date(1000L));
        when(streamRecord.getKeys()).thenReturn(createAttributeMap());
        when(streamRecord.getNewImage()).thenReturn(null);
        when(streamRecord.getOldImage()).thenReturn(null);
        when(streamRecord.getSequenceNumber()).thenReturn("123");
        when(streamRecord.getSizeBytes()).thenReturn(456L);
        when(streamRecord.getStreamViewType()).thenReturn(StreamViewType.NEW_AND_OLD_IMAGES);

        // When
        software.amazon.awssdk.services.dynamodb.model.StreamRecord result = 
            DynamodbStreamRecordTransformer.toStreamRecordV2(streamRecord);

        // Then
        assertEquals(Instant.ofEpochMilli(1000L), result.approximateCreationDateTime());
        assertEquals(DynamodbAttributeValueTransformer.toAttributeValueMapV2(createAttributeMap()), result.keys());
        assertNull(result.newImage());
        assertNull(result.oldImage());
        assertEquals("123", result.sequenceNumber());
        assertEquals(456L, result.sizeBytes().longValue());
        assertEquals(StreamViewType.NEW_AND_OLD_IMAGES, result.streamViewType());
    }

    @Test
    public void testToStreamRecordV2_WithNewImageAndOldImage() {
        // Given
        StreamRecord streamRecord = mock(StreamRecord.class);
        when(streamRecord.getApproximateCreationDateTime()).thenReturn(new java.util.Date(2000L));
        when(streamRecord.getKeys()).thenReturn(createAttributeMap());
        when(streamRecord.getNewImage()).thenReturn(createAttributeMap());
        when(streamRecord.getOldImage()).thenReturn(createAttributeMap());
        when(streamRecord.getSequenceNumber()).thenReturn("456");
        when(streamRecord.getSizeBytes()).thenReturn(789L);
        when(streamRecord.getStreamViewType()).thenReturn(StreamViewType.NEW_IMAGE_ONLY);

        // When
        software.amazon.awssdk.services.dynamodb.model.StreamRecord result = 
            DynamodbStreamRecordTransformer.toStreamRecordV2(streamRecord);

        // Then
        assertEquals(Instant.ofEpochMilli(2000L), result.approximateCreationDateTime());
        assertEquals(DynamodbAttributeValueTransformer.toAttributeValueMapV2(createAttributeMap()), result.keys());
        assertEquals(DynamodbAttributeValueTransformer.toAttributeValueMapV2(createAttributeMap()), result.newImage());
        assertEquals(DynamodbAttributeValueTransformer.toAttributeValueMapV2(createAttributeMap()), result.oldImage());
        assertEquals("456", result.sequenceNumber());
        assertEquals(789L, result.sizeBytes().longValue());
        assertEquals(StreamViewType.NEW_IMAGE_ONLY, result.streamViewType());
    }

    private Map<String, AttributeValue> createAttributeMap() {
        Map<String, AttributeValue> map = new HashMap<>();
        AttributeValue attributeValue = mock(AttributeValue.class);
        map.put("key1", attributeValue);
        return map;
    }
}
