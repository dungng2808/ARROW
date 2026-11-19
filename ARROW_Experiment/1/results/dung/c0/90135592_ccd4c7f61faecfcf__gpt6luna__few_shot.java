package org.zalando.nakadiproducer.transmission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class MockNakadiPublishingClient_ArrowTest {

    @Test
    public void publishesEventsAsJson() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        List<Object> events = new ArrayList<>();
        events.add(Collections.singletonMap("eventType", "created"));
        events.add(Collections.singletonMap("eventType", "updated"));

        client.publish("orders", events);

        assertEquals(
                Arrays.asList("{\"event_type\":\"created\"}", "{\"event_type\":\"updated\"}"),
                client.getSentEvents("orders"));
    }

    @Test
    public void returnsEmptyListForUnknownEventType() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();

        assertTrue(client.getSentEvents("unknown").isEmpty());
    }

    @Test
    public void clearRemovesPreviouslyPublishedEvents() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        client.publish("orders", Collections.singletonList(Collections.singletonMap("id", 1)));

        client.clearSentEvents();

        assertTrue(client.getSentEvents("orders").isEmpty());
    }

    @Test
    public void usesProvidedObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        MockNakadiPublishingClient client = new MockNakadiPublishingClient(mapper);
        client.publish("orders", Collections.singletonList(Collections.singletonMap("eventType", "created")));

        assertEquals(
                Collections.singletonList("{\"eventType\":\"created\"}"),
                client.getSentEvents("orders"));
    }

    @Test
    public void publishingEmptyListStoresNoEvents() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        client.publish("orders", Collections.emptyList());

        assertTrue(client.getSentEvents("orders").isEmpty());
    }
}
