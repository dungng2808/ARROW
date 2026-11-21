package org.zalando.nakadiproducer.transmission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class MockNakadiPublishingClient_ArrowTest {

    private MockNakadiPublishingClient client;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        client = new MockNakadiPublishingClient(objectMapper);
    }

    @Test
    public void testConstructorWithObjectMapper() {
        assertNotNull(client);
    }

    @Test
    public void testPublishEmptyList() {
        client.publish("test-event", new ArrayList<>());
        assertTrue(client.getSentEvents("test-event").isEmpty());
    }

    @Test
    public void testPublishSingleEvent() {
        TestEvent event = new TestEvent("id1", "data1");
        client.publish("test-event", Arrays.asList(event));

        List<String> sentEvents = client.getSentEvents("test-event");
        assertEquals(1, sentEvents.size());
        assertTrue(sentEvents.get(0).contains("\"id\":\"id1\""));
        assertTrue(sentEvents.get(0).contains("\"data\":\"data1\""));
    }

    @Test
    public void testPublishMultipleEvents() {
        TestEvent event1 = new TestEvent("id1", "data1");
        TestEvent event2 = new TestEvent("id2", "data2");
        client.publish("test-event", Arrays.asList(event1, event2));

        List<String> sentEvents = client.getSentEvents("test-event");
        assertEquals(2, sentEvents.size());
        assertTrue(sentEvents.get(0).contains("\"id\":\"id1\""));
        assertTrue(sentEvents.get(1).contains("\"id\":\"id2\""));
    }

    @Test
    public void testPublishDifferentEventTypes() {
        TestEvent event1 = new TestEvent("id1", "data1");
        TestEvent event2 = new TestEvent("id2", "data2");

        client.publish("event-type-1", Arrays.asList(event1));
        client.publish("event-type-2", Arrays.asList(event2));

        List<String> events1 = client.getSentEvents("event-type-1");
        List<String> events2 = client.getSentEvents("event-type-2");

        assertEquals(1, events1.size());
        assertEquals(1, events2.size());
        assertTrue(events1.get(0).contains("\"id\":\"id1\""));
        assertTrue(events2.get(0).contains("\"id\":\"id2\""));
    }

    @Test
    public void testGetSentEventsNonExistentEventType() {
        List<String> events = client.getSentEvents("non-existent");
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    public void testClearSentEvents() {
        TestEvent event = new TestEvent("id1", "data1");
        client.publish("test-event", Arrays.asList(event));
        assertFalse(client.getSentEvents("test-event").isEmpty());

        client.clearSentEvents();
        assertTrue(client.getSentEvents("test-event").isEmpty());
    }

    @Test
    public void testConcurrentAccess() {
        TestEvent event1 = new TestEvent("id1", "data1");
        TestEvent event2 = new TestEvent("id2", "data2");

        Thread t1 = new Thread(() -> client.publish("event-type-1", Arrays.asList(event1)));
        Thread t2 = new Thread(() -> client.publish("event-type-2", Arrays.asList(event2)));

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertEquals(1, client.getSentEvents("event-type-1").size());
        assertEquals(1, client.getSentEvents("event-type-2").size());
    }

    @Test
    public void testDefaultObjectMapperCreation() {
        MockNakadiPublishingClient clientWithDefault = new MockNakadiPublishingClient();
        assertNotNull(clientWithDefault);
    }

    private static class TestEvent {
        private String id;
        private String data;

        public TestEvent() {}

        public TestEvent(String id, String data) {
            this.id = id;
            this.data = data;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
