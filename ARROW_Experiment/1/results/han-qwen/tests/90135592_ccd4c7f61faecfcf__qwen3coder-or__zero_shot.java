package org.zalando.nakadiproducer.transmission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MockNakadiPublishingClient_ArrowTest {

    @Test
    public void testConstructorWithDefaultObjectMapper() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        assertNotNull(client);
    }

    @Test
    public void testConstructorWithCustomObjectMapper() {
        ObjectMapper customMapper = new ObjectMapper();
        MockNakadiPublishingClient client = new MockNakadiPublishingClient(customMapper);
        assertNotNull(client);
    }

    @Test
    public void testPublishSingleEvent() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        String eventType = "test-event";
        List<String> events = new ArrayList<>();
        events.add("event1");

        client.publish(eventType, events);

        List<String> sentEvents = client.getSentEvents(eventType);
        assertEquals(1, sentEvents.size());
        assertEquals("\"event1\"", sentEvents.get(0));
    }

    @Test
    public void testPublishMultipleEvents() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        String eventType = "test-event";
        List<String> events = new ArrayList<>();
        events.add("event1");
        events.add("event2");
        events.add("event3");

        client.publish(eventType, events);

        List<String> sentEvents = client.getSentEvents(eventType);
        assertEquals(3, sentEvents.size());
        assertEquals("\"event1\"", sentEvents.get(0));
        assertEquals("\"event2\"", sentEvents.get(1));
        assertEquals("\"event3\"", sentEvents.get(2));
    }

    @Test
    public void testGetSentEventsForNonExistentEventType() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        List<String> sentEvents = client.getSentEvents("non-existent");
        assertNotNull(sentEvents);
        assertTrue(sentEvents.isEmpty());
    }

    @Test
    public void testClearSentEvents() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        String eventType = "test-event";
        List<String> events = new ArrayList<>();
        events.add("event1");

        client.publish(eventType, events);
        List<String> sentEvents = client.getSentEvents(eventType);
        assertEquals(1, sentEvents.size());

        client.clearSentEvents();
        sentEvents = client.getSentEvents(eventType);
        assertTrue(sentEvents.isEmpty());
    }

    @Test
    public void testPublishEmptyList() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        String eventType = "test-event";
        List<String> events = new ArrayList<>();

        client.publish(eventType, events);

        List<String> sentEvents = client.getSentEvents(eventType);
        assertTrue(sentEvents.isEmpty());
    }

    @Test
    public void testPublishNullEvent() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        String eventType = "test-event";
        List<String> events = new ArrayList<>();
        events.add(null);

        client.publish(eventType, events);

        List<String> sentEvents = client.getSentEvents(eventType);
        assertEquals(1, sentEvents.size());
        assertEquals("null", sentEvents.get(0));
    }
}
