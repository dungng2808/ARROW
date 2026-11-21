package com.github.christophersmith.summer.mqtt.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TopicSubscription_ArrowTest
{
    @Test
    public void constructorStoresTopicAndQualityOfService()
    {
        TopicSubscription subscription =
                new TopicSubscription("sensors/temperature", MqttQualityOfService.values()[0]);

        assertEquals("sensors/temperature", subscription.getTopicFilter());
        assertSame(MqttQualityOfService.values()[0], subscription.getQualityOfService());
        assertFalse(subscription.isSubscribed());
    }

    @Test
    public void setSubscribedUpdatesSubscriptionState()
    {
        TopicSubscription subscription =
                new TopicSubscription("sensors/temperature", MqttQualityOfService.values()[0]);

        subscription.setSubscribed(true);
        assertTrue(subscription.isSubscribed());

        subscription.setSubscribed(false);
        assertFalse(subscription.isSubscribed());
    }

    @Test
    public void cloneCopiesValuesIntoIndependentInstance()
    {
        TopicSubscription subscription =
                new TopicSubscription("sensors/temperature", MqttQualityOfService.values()[0]);
        subscription.setSubscribed(true);

        TopicSubscription copy = subscription.clone();

        assertNotSame(subscription, copy);
        assertEquals("sensors/temperature", copy.getTopicFilter());
        assertSame(MqttQualityOfService.values()[0], copy.getQualityOfService());
        assertTrue(copy.isSubscribed());

        copy.setSubscribed(false);
        assertTrue(subscription.isSubscribed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullTopicFilter()
    {
        new TopicSubscription(null, MqttQualityOfService.values()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsEmptyTopicFilter()
    {
        new TopicSubscription("", MqttQualityOfService.values()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsWhitespaceTopicFilter()
    {
        new TopicSubscription("   ", MqttQualityOfService.values()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullQualityOfService()
    {
        new TopicSubscription("sensors/temperature", null);
    }
}
