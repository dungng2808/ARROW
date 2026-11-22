package org.apache.rocketmq.flink.common.selector;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;

public class SimpleTopicSelector_ArrowTest {

    @Test
    public void testGetTopicWithValidTopicField() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topic", "testTopic");
        tuple.put("tag", "testTag");

        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "defaultTopic", "tag", "defaultTag");
        String result = selector.getTopic(tuple);
        assertEquals("testTopic", result);
    }

    @Test
    public void testGetTopicWithNullTopicValue() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topic", null);
        tuple.put("tag", "testTag");

        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "defaultTopic", "tag", "defaultTag");
        String result = selector.getTopic(tuple);
        assertEquals("defaultTopic", result);
    }

    @Test
    public void testGetTopicWithMissingTopicField() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("tag", "testTag");

        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "defaultTopic", "tag", "defaultTag");
        String result = selector.getTopic(tuple);
        assertEquals("defaultTopic", result);
    }

    @Test
    public void testGetTagWithValidTagField() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topic", "testTopic");
        tuple.put("tag", "testTag");

        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "defaultTopic", "tag", "defaultTag");
        String result = selector.getTag(tuple);
        assertEquals("testTag", result);
    }

    @Test
    public void testGetTagWithNullTagValue() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topic", "testTopic");
        tuple.put("tag", null);

        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "defaultTopic", "tag", "defaultTag");
        String result = selector.getTag(tuple);
        assertEquals("defaultTag", result);
    }

    @Test
    public void testGetTagWithMissingTagField() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topic", "testTopic");

        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "defaultTopic", "tag", "defaultTag");
        String result = selector.getTag(tuple);
        assertEquals("defaultTag", result);
    }

    @Test
    public void testGetTopicWithEmptyStringTopic() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topic", "");
        tuple.put("tag", "testTag");

        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "defaultTopic", "tag", "defaultTag");
        String result = selector.getTopic(tuple);
        assertEquals("", result);
    }

    @Test
    public void testGetTagWithEmptyStringTag() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topic", "testTopic");
        tuple.put("tag", "");

        SimpleTopicSelector selector = new SimpleTopicSelector("topic", "defaultTopic", "tag", "defaultTag");
        String result = selector.getTag(tuple);
        assertEquals("", result);
    }
}
