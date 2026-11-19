package org.zalando.nakadiproducer.transmission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class MockNakadiPublishingClient_ArrowTest {

    private MockNakadiPublishingClient client;

    @Before
    public void setUp() {
        client = new MockNakadiPublishingClient();
    }

    @Test
    public void testConstructorWithObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        MockNakadiPublishingClient clientWithMapper = new MockNakadiPublishingClient(mapper);
        assertNotNull(clientWithMapper);
    }

    @Test
    public void testPublishSingleEvent() {
        Map<String, Object> event = new HashMap<>();
        event.put("id", "123");
        event.put("name", "test");

        client.publish("test-event-type", Collections.singletonList(event));

        List<String> sentEvents = client.getSentEvents("test-event-type");
        assertThat(sentEvents, hasSize(1));
        assertThat(sentEvents.get(0), containsString("\"id\":\"123\""));
        assertThat(sentEvents.get(0), containsString("\"name\":\"test\""));
    }

    @Test
    public void testPublishMultipleEvents() {
        Map<String, Object> event1 = new HashMap<>();
        event1.put("id", "1");
        event1.put("name", "first");

        Map<String, Object> event2 = new HashMap<>();
        event2.put("id", "2");
        event2.put("name", "second");

        client.publish("test-event-type", Arrays.asList(event1, event2));

        List<String> sentEvents = client.getSentEvents("test-event-type");
        assertThat(sentEvents, hasSize(2));
        assertThat(sentEvents.get(0), containsString("\"id\":\"1\""));
        assertThat(sentEvents.get(1), containsString("\"id\":\"2\""));
    }

    @Test
    public void testGetSentEventsForNonExistentEventType() {
        List<String> sentEvents = client.getSentEvents("non-existent-event-type");
        assertThat(sentEvents, hasSize(0));
    }

    @Test
    public void testGetSentEventsReturnsCopy() {
        Map<String, Object> event = new HashMap<>();
        event.put("id", "123");
        event.put("name", "test");

        client.publish("test-event-type", Collections.singletonList(event));

        List<String> sentEvents = client.getSentEvents("test-event-type");
        sentEvents.add("modified");

        List<String> sentEventsAgain = client.getSentEvents("test-event-type");
        assertThat(sentEventsAgain, hasSize(1));
    }

    @Test
    public void testClearSentEvents() {
        Map<String, Object> event = new HashMap<>();
        event.put("id", "123");
        event.put("name", "test");

        client.publish("test-event-type", Collections.singletonList(event));
        client.clearSentEvents();

        List<String> sentEvents = client.getSentEvents("test-event-type");
        assertThat(sentEvents, hasSize(0));
    }

    @Test
    public void testPublishDifferentEventTypes() {
        Map<String, Object> event1 = new HashMap<>();
        event1.put("id", "1");
        event1.put("name", "first");

        Map<String, Object> event2 = new HashMap<>();
        event2.put("id", "2");
        event2.put("name", "second");

        client.publish("event-type-1", Collections.singletonList(event1));
        client.publish("event-type-2", Collections.singletonList(event2));

        List<String> sentEvents1 = client.getSentEvents("event-type-1");
        List<String> sentEvents2 = client.getSentEvents("event-type-2");

        assertThat(sentEvents1, hasSize(1));
        assertThat(sentEvents2, hasSize(1));
        assertThat(sentEvents1.get(0), containsString("\"id\":\"1\""));
        assertThat(sentEvents2.get(0), containsString("\"id\":\"2\""));
    }

    @Test
    public void testEmptyList() {
        client.publish("test-event-type", Collections.emptyList());
        List<String> sentEvents = client.getSentEvents("test-event-type");
        assertThat(sentEvents, hasSize(0));
    }

    @Test
    public void testNullEventList() {
        // This should not throw an exception but rather handle gracefully
        client.publish("test-event-type", null);
        List<String> sentEvents = client.getSentEvents("test-event-type");
        assertThat(sentEvents, hasSize(0));
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        final List<String> results = new ArrayList<>();
        
        Runnable task = () -> {
            Map<String, Object> event = new HashMap<>();
            event.put("id", UUID.randomUUID().toString());
            event.put("name", "test");
            client.publish("concurrent-test", Collections.singletonList(event));
            results.add("completed");
        };

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(task);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(10, results.size());
        List<String> sentEvents = client.getSentEvents("concurrent-test");
        assertEquals(10, sentEvents.size());
    }
}
