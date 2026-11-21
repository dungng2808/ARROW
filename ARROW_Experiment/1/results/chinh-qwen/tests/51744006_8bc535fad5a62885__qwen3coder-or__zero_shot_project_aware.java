package com.orange.ngsi2.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.Instant;

public class Subscription_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        Subscription subscription = new Subscription();
        assertNull(subscription.getId());
        assertNull(subscription.getSubject());
        assertNull(subscription.getNotification());
        assertNull(subscription.getExpires());
        assertNull(subscription.getStatus());
    }

    @Test
    public void testParameterizedConstructor() {
        String id = "testId";
        SubjectSubscription subject = new SubjectSubscription();
        Notification notification = new Notification();
        Instant expires = Instant.now();
        Subscription.Status status = Subscription.Status.active;

        Subscription subscription = new Subscription(id, subject, notification, expires, status);

        assertEquals(id, subscription.getId());
        assertEquals(subject, subscription.getSubject());
        assertEquals(notification, subscription.getNotification());
        assertEquals(expires, subscription.getExpires());
        assertEquals(status, subscription.getStatus());
    }

    @Test
    public void testGetId() {
        Subscription subscription = new Subscription();
        assertNull(subscription.getId());

        String id = "testId";
        subscription.setId(id);
        assertEquals(id, subscription.getId());
    }

    @Test
    public void testSetId() {
        Subscription subscription = new Subscription();
        assertNull(subscription.getId());

        String id = "testId";
        subscription.setId(id);
        assertEquals(id, subscription.getId());
    }

    @Test
    public void testGetSubject() {
        Subscription subscription = new Subscription();
        assertNull(subscription.getSubject());

        SubjectSubscription subject = new SubjectSubscription();
        subscription.setSubject(subject);
        assertEquals(subject, subscription.getSubject());
    }

    @Test
    public void testSetSubject() {
        Subscription subscription = new Subscription();
        assertNull(subscription.getSubject());

        SubjectSubscription subject = new SubjectSubscription();
        subscription.setSubject(subject);
        assertEquals(subject, subscription.getSubject());
    }

    @Test
    public void testGetNotification() {
        Subscription subscription = new Subscription();
        assertNull(subscription.getNotification());

        Notification notification = new Notification();
        subscription.setNotification(notification);
        assertEquals(notification, subscription.getNotification());
    }

    @Test
    public void testSetNotification() {
        Subscription subscription = new Subscription();
        assertNull(subscription.getNotification());

        Notification notification = new Notification();
        subscription.setNotification(notification);
        assertEquals(notification, subscription.getNotification());
    }

    @Test
    public void testGetStatus() {
        Subscription subscription = new Subscription();
        assertNull(subscription.getStatus());

        Subscription.Status status = Subscription.Status.active;
        subscription.setStatus(status);
        assertEquals(status, subscription.getStatus());
    }

    @Test
    public void testSetStatus() {
        Subscription subscription = new Subscription();
        assertNull(subscription.getStatus());

        Subscription.Status status = Subscription.Status.active;
        subscription.setStatus(status);
        assertEquals(status, subscription.getStatus());
    }

    @Test
    public void testGetExpires() {
        Subscription subscription = new Subscription();
        assertNull(subscription.getExpires());

        Instant expires = Instant.now();
        subscription.setExpires(expires);
        assertEquals(expires, subscription.getExpires());
    }

    @Test
    public void testSetExpires() {
        Subscription subscription = new Subscription();
        assertNull(subscription.getExpires());

        Instant expires = Instant.now();
        subscription.setExpires(expires);
        assertEquals(expires, subscription.getExpires());
    }
}
