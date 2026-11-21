package org.zalando.nakadiproducer.transmission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MockNakadiPublishingClient_ArrowTest {

    @Test
    public void publishSerializesEventsAndPreservesTheirOrder() {
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
    public void publishAccumulatesEventsForAnEventType() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        client.publish("orders", Collections.singletonList("first"));
        client.publish("orders", Collections.singletonList("second"));

        assertEquals(Arrays.asList("\"first\"", "\"second\""), client.getSentEvents("orders"));
    }

    @Test
    public void publishKeepsDifferentEventTypesSeparate() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        client.publish("orders", Collections.singletonList("order"));
        client.publish("customers", Collections.singletonList("customer"));

        assertEquals(Collections.singletonList("\"order\""), client.getSentEvents("orders"));
        assertEquals(Collections.singletonList("\"customer\""), client.getSentEvents("customers"));
    }

    @Test
    public void getSentEventsReturnsAnIndependentList() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        client.publish("orders", Collections.singletonList("first"));

        List<String> events = client.getSentEvents("orders");
        events.clear();

        assertEquals(Collections.singletonList("\"first\""), client.getSentEvents("orders"));
    }

    @Test
    public void clearSentEventsRemovesAllEvents() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        client.publish("orders", Collections.singletonList("order"));
        client.publish("customers", Collections.singletonList("customer"));

        client.clearSentEvents();

        assertEquals(Collections.emptyList(), client.getSentEvents("orders"));
        assertEquals(Collections.emptyList(), client.getSentEvents("customers"));
    }

    @Test
    public void customObjectMapperIsUsedToSerializeEvents() {
        ObjectMapper mapper = new ObjectMapper();
        MockNakadiPublishingClient client = new MockNakadiPublishingClient(mapper);
        client.publish("values", Collections.singletonList(Collections.singletonMap("key", "value")));

        assertEquals(Collections.singletonList("{\"key\":\"value\"}"), client.getSentEvents("values"));
    }

    @Test
    public void publishSerializesNullEvent() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        client.publish("values", Collections.<Object>singletonList(null));

        assertEquals(Collections.singletonList("null"), client.getSentEvents("values"));
    }

    @Test
    public void publishSerializesBeanPropertiesUsingSnakeCaseAndOmitsNulls() {
        MockNakadiPublishingClient client = new MockNakadiPublishingClient();
        client.publish("values", Collections.singletonList(new SampleEvent("hello", null)));

        assertEquals(Collections.singletonList("{\"event_name\":\"hello\"}"), client.getSentEvents("values"));
    }

    public static class SampleEvent {
        private final String eventName;
        private final String optionalValue;

        public SampleEvent(String eventName, String optionalValue) {
            this.eventName = eventName;
            this.optionalValue = optionalValue;
        }

        public String getEventName() {
            return eventName;
        }

        public String getOptionalValue() {
            return optionalValue;
        }
    }
}
