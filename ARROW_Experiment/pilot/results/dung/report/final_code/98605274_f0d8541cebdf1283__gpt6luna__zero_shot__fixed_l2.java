package com.github.christophersmith.summer.mqtt.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TopicSubscription_ArrowTest
{
    private MqttQualityOfService qualityOfService(int level)
    {
        return MqttQualityOfService.findByLevelIdentifier(level);
    }

    @Test
    public void constructorStoresTopicAndQualityOfService()
    {
        MqttQualityOfService qos = qualityOfService(0);
        TopicSubscription subscription =
                new TopicSubscription("sensors/temperature", qos);

        assertEquals("sensors/temperature", subscription.getTopicFilter());
        assertSame(qos, subscription.getQualityOfService());
        assertFalse(subscription.isSubscribed());
    }

    @Test
    public void setSubscribedUpdatesSubscriptionState()
    {
        TopicSubscription subscription =
                new TopicSubscription("sensors/temperature", qualityOfService(1));

        subscription.setSubscribed(true);
        assertTrue(subscription.isSubscribed());

        subscription.setSubscribed(false);
        assertFalse(subscription.isSubscribed());
    }

    @Test
    public void cloneCopiesValuesIntoDistinctInstance()
    {
        MqttQualityOfService qos = qualityOfService(2);
        TopicSubscription subscription =
                new TopicSubscription("sensors/temperature", qos);
        subscription.setSubscribed(true);

        TopicSubscription copy = subscription.clone();

        assertNotSame(subscription, copy);
        assertEquals("sensors/temperature", copy.getTopicFilter());
        assertSame(qos, copy.getQualityOfService());
        assertTrue(copy.isSubscribed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullTopicFilter()
    {
        new TopicSubscription(null, qualityOfService(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsEmptyTopicFilter()
    {
        new TopicSubscription("", qualityOfService(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsWhitespaceTopicFilter()
    {
        new TopicSubscription("   ", qualityOfService(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullQualityOfService()
    {
        new TopicSubscription("sensors/temperature", null);
    }
}
