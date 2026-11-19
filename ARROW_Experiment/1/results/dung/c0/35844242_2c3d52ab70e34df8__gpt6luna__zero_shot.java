package com.amazonaws.services.lambda.runtime.events.transformers.dynamodb;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DynamodbStreamRecordTransformer_ArrowTest {

    @Test
    public void transformsStreamRecordFields() {
        StreamRecord input = new StreamRecord();
        Date creationTime = new Date(123456789L);
        input.setApproximateCreationDateTime(creationTime);
        input.setKeys(new HashMap<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue>());
        input.setNewImage(null);
        input.setOldImage(null);
        input.setSequenceNumber("sequence-1");
        input.setSizeBytes(42L);
        input.setStreamViewType("NEW_AND_OLD_IMAGES");

        software.amazon.awssdk.services.dynamodb.model.StreamRecord result =
                DynamodbStreamRecordTransformer.toStreamRecordV2(input);

        assertEquals(creationTime.toInstant(), result.approximateCreationDateTime());
        assertEquals("sequence-1", result.sequenceNumber());
        assertEquals(Long.valueOf(42L), result.sizeBytes());
        assertEquals("NEW_AND_OLD_IMAGES", result.streamViewTypeAsString());
        assertEquals(new HashMap<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue>(), result.keys());
        assertNull(result.newImage());
        assertNull(result.oldImage());
    }

    @Test
    public void transformsEmptyMapsAsEmptyMaps() {
        StreamRecord input = new StreamRecord();
        input.setApproximateCreationDateTime(new Date(0L));
        input.setKeys(new HashMap<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue>());
        input.setNewImage(new HashMap<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue>());
        input.setOldImage(new HashMap<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue>());

        software.amazon.awssdk.services.dynamodb.model.StreamRecord result =
                DynamodbStreamRecordTransformer.toStreamRecordV2(input);

        assertEquals(new HashMap<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue>(), result.newImage());
        assertEquals(new HashMap<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue>(), result.oldImage());
    }
}
