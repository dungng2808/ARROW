package com.github.christophersmith.summer.mqtt.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class TopicSubscription_ArrowTest
{
    @Test
    public void testConstructorValidInputs()
    {
        TopicSubscription subscription = new TopicSubscription("test/topic", MqttQualityOfService.findByLevelIdentifier(2));
        assertEquals("test/topic", subscription.getTopicFilter());
        assertEquals(MqttQualityOfService.findByLevelIdentifier(2), subscription.getQualityOfService());
        assertFalse(subscription.isSubscribed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullTopicFilter()
    {
        new TopicSubscription(null, MqttQualityOfService.findByLevelIdentifier(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyTopicFilter()
    {
        new TopicSubscription("", MqttQualityOfService.findByLevelIdentifier(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullQualityOfService()
    {
        new TopicSubscription("test/topic", null);
    }

    @Test
    public void testGetTopicFilter()
    {
        TopicSubscription subscription = new TopicSubscription("test/topic", MqttQualityOfService.findByLevelIdentifier(0));
        assertEquals("test/topic", subscription.getTopicFilter());
    }

    @Test
    public void testGetQualityOfService()
    {
        TopicSubscription subscription = new TopicSubscription("test/topic", MqttQualityOfService.findByLevelIdentifier(1));
        assertEquals(MqttQualityOfService.findByLevelIdentifier(1), subscription.getQualityOfService());
    }

    @Test
    public void testIsSubscribedInitiallyFalse()
    {
        TopicSubscription subscription = new TopicSubscription("test/topic", MqttQualityOfService.findByLevelIdentifier(2));
        assertFalse(subscription.isSubscribed());
    }

    @Test
    public void testSetSubscribedTrue()
    {
        TopicSubscription subscription = new TopicSubscription("test/topic", MqttQualityOfService.findByLevelIdentifier(2));
        subscription.setSubscribed(true);
        assertTrue(subscription.isSubscribed());
    }

    @Test
    public void testSetSubscribedFalse()
    {
        TopicSubscription subscription = new TopicSubscription("test/topic", MqttQualityOfService.findByLevelIdentifier(2));
        subscription.setSubscribed(true);
        subscription.setSubscribed(false);
        assertFalse(subscription.isSubscribed());
    }

    @Test
    public void testClone()
    {
        TopicSubscription original = new TopicSubscription("test/topic", MqttQualityOfService.findByLevelIdentifier(0));
        original.setSubscribed(true);
        
        TopicSubscription cloned = original.clone();
        
        assertEquals(original.getTopicFilter(), cloned.getTopicFilter());
        assertEquals(original.getQualityOfService(), cloned.getQualityOfService());
        assertEquals(original.isSubscribed(), cloned.isSubscribed());
        
        // Verify they are different instances
        assertNotSame(original, cloned);
    }

    @Test
    public void testCloneUnsubscribed()
    {
        TopicSubscription original = new TopicSubscription("test/topic", MqttQualityOfService.findByLevelIdentifier(0));
        // Don't set subscribed to true
        
        TopicSubscription cloned = original.clone();
        
        assertEquals(original.getTopicFilter(), cloned.getTopicFilter());
        assertEquals(original.getQualityOfService(), cloned.getQualityOfService());
        assertEquals(original.isSubscribed(), cloned.isSubscribed());
        
        // Verify they are different instances
        assertNotSame(original, cloned);
    }
}
