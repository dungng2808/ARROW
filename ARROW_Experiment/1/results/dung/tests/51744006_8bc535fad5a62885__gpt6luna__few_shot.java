package com.orange.ngsi2.model;

import java.time.Instant;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class Subscription_ArrowTest {

    @Test
    public void noArgumentConstructorInitializesFieldsToNull() {
        Subscription subscription = new Subscription();

        assertNull(subscription.getId());
        assertNull(subscription.getSubject());
        assertNull(subscription.getNotification());
        assertNull(subscription.getExpires());
        assertNull(subscription.getStatus());
    }

    @Test
    public void constructorStoresProvidedValues() {
        Instant expires = Instant.parse("2024-01-02T03:04:05Z");
        Subscription subscription = new Subscription(
                "subscription-1", null, null, expires, Subscription.Status.active);

        assertEquals("subscription-1", subscription.getId());
        assertNull(subscription.getSubject());
        assertNull(subscription.getNotification());
        assertSame(expires, subscription.getExpires());
        assertEquals(Subscription.Status.active, subscription.getStatus());
    }

    @Test
    public void settersUpdateValues() {
        Subscription subscription = new Subscription();
        Instant expires = Instant.parse("2024-01-02T03:04:05Z");

        subscription.setId("subscription-2");
        subscription.setSubject(null);
        subscription.setNotification(null);
        subscription.setExpires(expires);
        subscription.setStatus(Subscription.Status.expired);

        assertEquals("subscription-2", subscription.getId());
        assertNull(subscription.getSubject());
        assertNull(subscription.getNotification());
        assertSame(expires, subscription.getExpires());
        assertEquals(Subscription.Status.expired, subscription.getStatus());
    }
}
