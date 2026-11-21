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
    public void constructorInitializesAllFields() {
        Instant expires = Instant.parse("2020-01-02T03:04:05Z");
        Subscription subscription = new Subscription(
                "subscription-1", null, null, expires, Subscription.Status.active);

        assertEquals("subscription-1", subscription.getId());
        assertNull(subscription.getSubject());
        assertNull(subscription.getNotification());
        assertSame(expires, subscription.getExpires());
        assertEquals(Subscription.Status.active, subscription.getStatus());
    }

    @Test
    public void setIdUpdatesId() {
        Subscription subscription = new Subscription();
        subscription.setId("subscription-2");

        assertEquals("subscription-2", subscription.getId());
    }

    @Test
    public void setSubjectUpdatesSubject() {
        Subscription subscription = new Subscription();
        subscription.setSubject(null);

        assertNull(subscription.getSubject());
    }

    @Test
    public void setNotificationUpdatesNotification() {
        Subscription subscription = new Subscription();
        subscription.setNotification(null);

        assertNull(subscription.getNotification());
    }

    @Test
    public void setStatusUpdatesStatus() {
        Subscription subscription = new Subscription();
        subscription.setStatus(Subscription.Status.expired);

        assertEquals(Subscription.Status.expired, subscription.getStatus());
    }

    @Test
    public void setExpiresUpdatesExpires() {
        Subscription subscription = new Subscription();
        Instant expires = Instant.parse("2021-06-07T08:09:10Z");
        subscription.setExpires(expires);

        assertSame(expires, subscription.getExpires());
    }
}
