package org.apache.rocketmq.flink.common.selector;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SimpleTopicSelector_ArrowTest {

    @Test
    public void testGetTopicWithExistingField() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topicField", "testTopic");

        SimpleTopicSelector selector = new SimpleTopicSelector("topicField", "defaultTopic", "tagField", "defaultTag");
        String result = selector.getTopic(tuple);
        assertEquals("testTopic", result);
    }

    @Test
    public void testGetTopicWithNullFieldValue() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topicField", null);

        SimpleTopicSelector selector = new SimpleTopicSelector("topicField", "defaultTopic", "tagField", "defaultTag");
        String result = selector.getTopic(tuple);
        assertEquals("defaultTopic", result);
    }

    @Test
    public void testGetTopicWithMissingField() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("otherField", "value");

        SimpleTopicSelector selector = new SimpleTopicSelector("topicField", "defaultTopic", "tagField", "defaultTag");
        String result = selector.getTopic(tuple);
        assertEquals("defaultTopic", result);
    }

    @Test
    public void testGetTagWithExistingField() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("tagField", "testTag");

        SimpleTopicSelector selector = new SimpleTopicSelector("topicField", "defaultTopic", "tagField", "defaultTag");
        String result = selector.getTag(tuple);
        assertEquals("testTag", result);
    }

    @Test
    public void testGetTagWithNullFieldValue() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("tagField", null);

        SimpleTopicSelector selector = new SimpleTopicSelector("topicField", "defaultTopic", "tagField", "defaultTag");
        String result = selector.getTag(tuple);
        assertEquals("defaultTag", result);
    }

    @Test
    public void testGetTagWithMissingField() {
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("otherField", "value");

        SimpleTopicSelector selector = new SimpleTopicSelector("topicField", "defaultTopic", "tagField", "defaultTag");
        String result = selector.getTag(tuple);
        assertEquals("defaultTag", result);
    }

    @Test
    public void testConstructorWithAllParameters() {
        SimpleTopicSelector selector = new SimpleTopicSelector("topicField", "defaultTopic", "tagField", "defaultTag");
        
        // Verify constructor parameters are stored correctly
        // Note: We cannot directly access private fields, but we can verify behavior
        Map<String, Object> tuple = new HashMap<>();
        tuple.put("topicField", "customTopic");
        tuple.put("tagField", "customTag");
        
        assertEquals("customTopic", selector.getTopic(tuple));
        assertEquals("customTag", selector.getTag(tuple));
    }
}
