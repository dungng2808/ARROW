package com.automationrockstars.bmo.event.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.automationrockstars.gunter.EventType;
import com.automationrockstars.gunter.events.Event;

public class SelectiveActionBuilder_ArrowTest {

    @Test
    public void predicateReturnsConfiguredValue() {
        assertEquals(Boolean.TRUE, SelectiveActionBuilder.predicate(true).apply(null));
        assertEquals(Boolean.FALSE, SelectiveActionBuilder.predicate(false).apply(null));
    }

    @Test
    public void newRuleCreatesBuilder() {
        assertNotNull(SelectiveActionBuilder.newRule());
    }

    @Test
    public void storeActionReturnsMessage() {
        Event event = new Event() { };
        Message result = SelectiveActionBuilder.newRule().on(EventType.ALL).store().process(event);
        assertNotNull(result);
    }

    @Test
    public void ignoreActionReturnsMessage() {
        Event event = new Event() { };
        Message result = SelectiveActionBuilder.newRule().on(EventType.ALL).ignore().process(event);
        assertNotNull(result);
    }

    @Test
    public void passThroughActionReturnsMessage() {
        Event event = new Event() { };
        Message result = SelectiveActionBuilder.newRule().on(EventType.ALL).passTrough().process(event);
        assertNotNull(result);
    }

    @Test
    public void firstMatchingActionIsUsed() {
        Event event = new Event() { };
        Message result = SelectiveActionBuilder.newRule()
                .on(SelectiveActionBuilder.predicate(false)).store()
                .on(EventType.ALL).ignore()
                .process(event);
        assertNotNull(result);
    }

    @Test
    public void unmatchedEventReturnsNullMessage() {
        Event event = new Event() { };
        Message result = SelectiveActionBuilder.newRule()
                .on(SelectiveActionBuilder.predicate(false)).store()
                .process(event);
        assertSame(Message.NULL, result);
    }
}
