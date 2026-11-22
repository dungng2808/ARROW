package org.apache.rocketmq.flink.common.selector;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleTopicSelector_ArrowTest {

    @Test
    public void getTopicUsesConfiguredFieldValue() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topic", "orders");

        assertEquals("orders", selector.getTopic(tuple));
    }

    @Test
    public void getTopicConvertsFieldValueToString() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topic", 42);

        assertEquals("42", selector.getTopic(tuple));
    }

    @Test
    public void getTopicUsesDefaultWhenFieldIsNull() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topic", null);

        assertEquals("default-topic", selector.getTopic(tuple));
    }

    @Test
    public void getTopicUsesDefaultWhenFieldIsMissing() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<>();

        assertEquals("default-topic", selector.getTopic(tuple));
    }

    @Test
    public void getTagUsesConfiguredFieldValue() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("tag", "priority");

        assertEquals("priority", selector.getTag(tuple));
    }

    @Test
    public void getTagConvertsFieldValueToString() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("tag", 7);

        assertEquals("7", selector.getTag(tuple));
    }

    @Test
    public void getTagUsesDefaultWhenFieldIsNull() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("tag", null);

        assertEquals("default-tag", selector.getTag(tuple));
    }

    @Test
    public void getTagUsesDefaultWhenFieldIsMissing() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<>();

        assertEquals("default-tag", selector.getTag(tuple));
    }
}
