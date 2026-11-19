package com.amazonaws.services.lambda.runtime.events.transformers.dynamodb;

import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import java.time.Instant;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DynamodbStreamRecordTransformer_ArrowTest {

    @Test
    public void transformsStreamRecordFields() {
        StreamRecord input = new StreamRecord();
        input.setApproximateCreationDateTime(Date.from(Instant.parse("2020-01-02T03:04:05Z")));
        input.setSequenceNumber("sequence-1");
        input.setSizeBytes(42L);
        input.setStreamViewType("NEW_AND_OLD_IMAGES");

        software.amazon.awssdk.services.dynamodb.model.StreamRecord result =
                DynamodbStreamRecordTransformer.toStreamRecordV2(input);

        assertEquals(Instant.parse("2020-01-02T03:04:05Z"), result.approximateCreationDateTime());
        assertEquals("sequence-1", result.sequenceNumber());
        assertEquals(Long.valueOf(42L), result.sizeBytes());
        assertEquals("NEW_AND_OLD_IMAGES", result.streamViewTypeAsString());
        assertNull(result.newImage());
        assertNull(result.oldImage());
    }
}
