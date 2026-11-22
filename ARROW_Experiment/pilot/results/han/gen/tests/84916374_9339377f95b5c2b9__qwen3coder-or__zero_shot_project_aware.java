package org.apache.rocketmq.flink.common.selector;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;

public class SimpleTopicSelector_ArrowTest {

    @Test
    public void testGetTopicWithValidField() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topic", "test-topic");
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        assertEquals("test-topic", selector.getTopic(tuple));
    }

    @Test
    public void testGetTopicWithNullValue() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topic", null);
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        assertEquals("default-topic", selector.getTopic(tuple));
    }

    @Test
    public void testGetTopicWithMissingField() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("other-field", "value");
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        assertEquals("default-topic", selector.getTopic(tuple));
    }

    @Test
    public void testGetTagWithValidField() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("tag", "test-tag");
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        assertEquals("test-tag", selector.getTag(tuple));
    }

    @Test
    public void testGetTagWithNullValue() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("tag", null);
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        assertEquals("default-tag", selector.getTag(tuple));
    }

    @Test
    public void testGetTagWithMissingField() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("other-field", "value");
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        assertEquals("default-tag", selector.getTag(tuple));
    }

    @Test
    public void testConstructor() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        assertNotNull(selector);
    }
}
