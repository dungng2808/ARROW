package com.automationrockstars.bmo.event.processor;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.automationrockstars.gunter.EventType;
import com.automationrockstars.gunter.events.Event;

public class SelectiveActionBuilder_ArrowTest {

    @Test
    public void newRuleReturnsBuilder() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        assertNotNull(builder);
    }

    @Test
    public void predicateReturnsConfiguredResult() {
        assertSame(Boolean.TRUE, SelectiveActionBuilder.predicate(true).apply(null));
        assertSame(Boolean.FALSE, SelectiveActionBuilder.predicate(false).apply(null));
    }

    @Test
    public void onOthersCanStoreAnEvent() {
        Event event = new Event() {};
        Message result = SelectiveActionBuilder.newRule().onOthers().store().process(event);
        assertSame(Message.NULL, result);
    }

    @Test
    public void ignoreReturnsNullMessage() {
        Event event = new Event() {};
        Message result = SelectiveActionBuilder.newRule().on(EventType.ALL).ignore().process(event);
        assertSame(Message.NULL, result);
    }

    @Test
    public void passThroughReturnsNullMessage() {
        Event event = new Event() {};
        Message result = SelectiveActionBuilder.newRule().on(EventType.ALL).passTrough().process(event);
        assertSame(Message.NULL, result);
    }
}
