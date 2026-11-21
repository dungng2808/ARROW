package com.github.christophersmith.summer.mqtt.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class TopicSubscription_ArrowTest
{
    @Test
    public void testConstructorValidInputs()
    {
        TopicSubscription subscription = new TopicSubscription("test/topic", MqttQualityOfService.EXACTLY_ONCE);
        assertEquals("test/topic", subscription.getTopicFilter());
        assertEquals(MqttQualityOfService.EXACTLY_ONCE, subscription.getQualityOfService());
        assertFalse(subscription.isSubscribed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullTopicFilter()
    {
        new TopicSubscription(null, MqttQualityOfService.EXACTLY_ONCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyTopicFilter()
    {
        new TopicSubscription("", MqttQualityOfService.EXACTLY_ONCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullQualityOfService()
    {
        new TopicSubscription("test/topic", null);
    }

    @Test
    public void testGetTopicFilter()
    {
        TopicSubscription subscription = new TopicSubscription("test/topic", MqttQualityOfService.AT_MOST_ONCE);
        assertEquals("test/topic", subscription.getTopicFilter());
    }

    @Test
    public void testGetQualityOfService()
    {
        TopicSubscription subscription = new TopicSubscription("test/topic", MqttQualityOfService.AT_LEAST_ONCE);
        assertEquals(MqttQualityOfService.AT_LEAST_ONCE, subscription.getQualityOfService());
    }

    @Test
    public void testIsSubscribedInitiallyFalse()
    {
        TopicSubscription subscription = new TopicSubscription("test/topic", MqttQualityOfService.EXACTLY_ONCE);
        assertFalse(subscription.isSubscribed());
    }

    @Test
    public void testSetSubscribedTrue()
    {
        TopicSubscription subscription = new TopicSubscription("test/topic", MqttQualityOfService.EXACTLY_ONCE);
        subscription.setSubscribed(true);
        assertTrue(subscription.isSubscribed());
    }

    @Test
    public void testSetSubscribedFalse()
    {
        TopicSubscription subscription = new TopicSubscription("test/topic", MqttQualityOfService.EXACTLY_ONCE);
        subscription.setSubscribed(true);
        subscription.setSubscribed(false);
        assertFalse(subscription.isSubscribed());
    }

    @Test
    public void testClone()
    {
        TopicSubscription original = new TopicSubscription("test/topic", MqttQualityOfService.EXACTLY_ONCE);
        original.setSubscribed(true);
        
        TopicSubscription cloned = original.clone();
        
        assertEquals(original.getTopicFilter(), cloned.getTopicFilter());
        assertEquals(original.getQualityOfService(), cloned.getQualityOfService());
        assertEquals(original.isSubscribed(), cloned.isSubscribed());
        
        // Ensure they are different objects
        assertNotSame(original, cloned);
    }

    @Test
    public void testCloneUnsubscribed()
    {
        TopicSubscription original = new TopicSubscription("test/topic", MqttQualityOfService.AT_MOST_ONCE);
        
        TopicSubscription cloned = original.clone();
        
        assertEquals(original.getTopicFilter(), cloned.getTopicFilter());
        assertEquals(original.getQualityOfService(), cloned.getQualityOfService());
        assertEquals(original.isSubscribed(), cloned.isSubscribed());
        
        // Ensure they are different objects
        assertNotSame(original, cloned);
    }
}
