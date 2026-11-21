package com.orange.ngsi2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.time.Instant;
import org.junit.Test;

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
        SubjectSubscription subject = null;
        Notification notification = null;
        Instant expires = Instant.parse("2024-01-02T03:04:05Z");
        Subscription.Status status = Subscription.Status.active;

        Subscription subscription = new Subscription("subscription-1", subject, notification, expires, status);

        assertEquals("subscription-1", subscription.getId());
        assertSame(subject, subscription.getSubject());
        assertSame(notification, subscription.getNotification());
        assertSame(expires, subscription.getExpires());
        assertSame(status, subscription.getStatus());
    }

    @Test
    public void settersUpdateAndGettersReturnValues() {
        Subscription subscription = new Subscription();
        Instant expires = Instant.parse("2025-06-07T08:09:10Z");
        Subscription.Status status = Subscription.Status.expired;

        subscription.setId("subscription-2");
        subscription.setSubject(null);
        subscription.setNotification(null);
        subscription.setExpires(expires);
        subscription.setStatus(status);

        assertEquals("subscription-2", subscription.getId());
        assertNull(subscription.getSubject());
        assertNull(subscription.getNotification());
        assertSame(expires, subscription.getExpires());
        assertSame(status, subscription.getStatus());
    }
}
