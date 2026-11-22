package org.apache.rocketmq.flink.common.selector;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleTopicSelector_ArrowTest {

    @Test
    public void getTopicReturnsFieldValueAsString() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<String, Object>();
        tuple.put("topic", 123);

        assertEquals("123", selector.getTopic(tuple));
    }

    @Test
    public void getTopicReturnsDefaultWhenFieldIsMissing() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<String, Object>();

        assertEquals("default-topic", selector.getTopic(tuple));
    }

    @Test
    public void getTopicReturnsDefaultWhenFieldValueIsNull() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<String, Object>();
        tuple.put("topic", null);

        assertEquals("default-topic", selector.getTopic(tuple));
    }

    @Test
    public void getTagReturnsFieldValueAsString() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<String, Object>();
        tuple.put("tag", 456);

        assertEquals("456", selector.getTag(tuple));
    }

    @Test
    public void getTagReturnsDefaultWhenFieldIsMissing() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<String, Object>();

        assertEquals("default-tag", selector.getTag(tuple));
    }

    @Test
    public void getTagReturnsDefaultWhenFieldValueIsNull() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "default-topic", "tag", "default-tag");
        Map<String, Object> tuple = new HashMap<String, Object>();
        tuple.put("tag", null);

        assertEquals("default-tag", selector.getTag(tuple));
    }
}
