package com.amazonaws.services.lambda.runtime.events.transformers.dynamodb;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DynamodbStreamRecordTransformer_ArrowTest {

    @Test
    public void convertsStreamRecordFields() {
        StreamRecord input = new StreamRecord();
        input.setApproximateCreationDateTime(new Date(123456789L));
        input.setKeys(new HashMap<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue>());
        input.setNewImage(new HashMap<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue>());
        input.setOldImage(new HashMap<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue>());
        input.setSequenceNumber("sequence-1");
        input.setSizeBytes(42L);
        input.setStreamViewType("NEW_AND_OLD_IMAGES");

        software.amazon.awssdk.services.dynamodb.model.StreamRecord result =
                DynamodbStreamRecordTransformer.toStreamRecordV2(input);

        assertEquals(java.time.Instant.ofEpochMilli(123456789L), result.approximateCreationDateTime());
        assertEquals("sequence-1", result.sequenceNumber());
        assertEquals(Long.valueOf(42L), result.sizeBytes());
        assertEquals("NEW_AND_OLD_IMAGES", result.streamViewTypeAsString());
        assertEquals(new HashMap<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue>(), result.keys());
        assertEquals(new HashMap<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue>(), result.newImage());
        assertEquals(new HashMap<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue>(), result.oldImage());
    }

    @Test
    public void leavesOptionalImagesNull() {
        StreamRecord input = new StreamRecord();
        input.setApproximateCreationDateTime(new Date(0L));
        input.setKeys(new HashMap<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue>());
        input.setSequenceNumber("sequence-2");
        input.setSizeBytes(0L);
        input.setStreamViewType("KEYS_ONLY");

        software.amazon.awssdk.services.dynamodb.model.StreamRecord result =
                DynamodbStreamRecordTransformer.toStreamRecordV2(input);

        assertNull(result.newImage());
        assertNull(result.oldImage());
        assertEquals(java.time.Instant.EPOCH, result.approximateCreationDateTime());
        assertEquals("KEYS_ONLY", result.streamViewTypeAsString());
    }
}
