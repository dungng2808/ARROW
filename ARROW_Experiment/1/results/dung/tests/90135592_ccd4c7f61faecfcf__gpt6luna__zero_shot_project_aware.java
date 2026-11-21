package org.zalando.nakadiproducer.transmission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MockNakadiPublishingClient_ArrowTest {

    @Test
    public void publishSerializesEventsAndReturnsThemByEventType() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();

        client.publish("orders", Arrays.asList("first", "second"));

        assertEquals(Arrays.asList("\"first\"", "\"second\""), client.getSentEvents("orders"));
    }

    @Test
    public void getSentEventsReturnsEmptyListForUnknownEventType() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();

        assertEquals(Collections.emptyList(), client.getSentEvents("unknown"));
    }

    @Test
    public void publishWithEmptyEventsDoesNotAddSentEvents() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();

        client.publish("orders", Collections.emptyList());

        assertEquals(Collections.emptyList(), client.getSentEvents("orders"));
    }

    @Test
    public void clearSentEventsRemovesPreviouslyPublishedEvents() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        client.publish("orders", Collections.singletonList("first"));

        client.clearSentEvents();

        assertEquals(Collections.emptyList(), client.getSentEvents("orders"));
    }

    @Test
    public void publishUsesProvidedObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        MockNakadiPublishingClient client = new MockNakadiPublishingClient(mapper);

        client.publish("orders", Collections.singletonList(Collections.singletonMap("orderId", 7)));

        assertEquals(Collections.singletonList("{\"orderId\":7}"), client.getSentEvents("orders"));
    }
}
