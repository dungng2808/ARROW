package com.github.christophersmith.summer.mqtt.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TopicSubscription_ArrowTest {

    private MqttQualityOfService qualityOfService(int levelIdentifier) {
        return MqttQualityOfService.findByLevelIdentifier(levelIdentifier);
    }

    @Test
    public void constructorStoresTopicAndQualityOfService() {
        MqttQualityOfService quality = qualityOfService(1);
        TopicSubscription subscription =
                new TopicSubscription("sensors/temperature", quality);

        assertEquals("sensors/temperature", subscription.getTopicFilter());
        assertSame(quality, subscription.getQualityOfService());
        assertFalse(subscription.isSubscribed());
    }

    @Test
    public void setSubscribedUpdatesSubscriptionState() {
        TopicSubscription subscription =
                new TopicSubscription("sensors/temperature", qualityOfService(0));

        subscription.setSubscribed(true);
        assertTrue(subscription.isSubscribed());

        subscription.setSubscribed(false);
        assertFalse(subscription.isSubscribed());
    }

    @Test
    public void cloneCopiesValuesIntoDistinctInstance() {
        MqttQualityOfService quality = qualityOfService(2);
        TopicSubscription subscription =
                new TopicSubscription("sensors/temperature", quality);
        subscription.setSubscribed(true);

        TopicSubscription copy = subscription.clone();

        assertNotSame(subscription, copy);
        assertEquals("sensors/temperature", copy.getTopicFilter());
        assertSame(quality, copy.getQualityOfService());
        assertTrue(copy.isSubscribed());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullTopicFilter() {
        new TopicSubscription(null, qualityOfService(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsEmptyTopicFilter() {
        new TopicSubscription("", qualityOfService(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullQualityOfService() {
        new TopicSubscription("sensors/temperature", null);
    }
}
