package com.automationrockstars.bmo.event.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.automationrockstars.gunter.EventType;
import com.automationrockstars.gunter.events.Event;
import com.automationrockstars.gunter.events.impl.EventImplUtils;
import com.google.common.base.Predicate;

public class SelectiveActionBuilder_ArrowTest {

    @Test
    public void newRuleCreatesBuilder() {
        assertNotNull(SelectiveActionBuilder.newRule());
    }

    @Test
    public void predicateReturnsConfiguredTrueValue() {
        Predicate<Event> predicate = SelectiveActionBuilder.predicate(true);
        assertEquals(true, predicate.apply(null));
    }

    @Test
    public void predicateReturnsConfiguredFalseValue() {
        Predicate<Event> predicate = SelectiveActionBuilder.predicate(false);
        assertEquals(false, predicate.apply(null));
    }

    @Test
    public void onPredicateReturnsSameBuilder() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        Predicate<Event> predicate = SelectiveActionBuilder.predicate(true);
        assertSame(builder, builder.on(predicate));
    }

    @Test
    public void onAllReturnsSameBuilder() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        assertSame(builder, builder.on(EventType.ALL));
    }

    @Test
    public void onOthersReturnsSameBuilder() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        assertSame(builder, builder.onOthers());
    }

    @Test
    public void onEventClassReturnsSameBuilder() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        assertSame(builder, builder.on(Event.class));
    }

    @Test
    public void ignoreReturnsSameBuilder() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        assertSame(builder, builder.ignore());
    }

    @Test
    public void storeReturnsSameBuilder() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        assertSame(builder, builder.store());
    }

    @Test
    public void passTroughReturnsSameBuilder() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        assertSame(builder, builder.passTrough());
    }

    @Test
    public void processWithNoActionsReturnsNullMessage() {
        Message result = SelectiveActionBuilder.newRule().process(null);
        assertSame(Message.NULL, result);
    }

    @Test
    public void processWithFalsePredicateReturnsNullMessage() {
        Message result = SelectiveActionBuilder.newRule()
                .on(SelectiveActionBuilder.predicate(false))
                .ignore()
                .process(null);
        assertSame(Message.NULL, result);
    }

    @Test
    public void processWithTruePredicateAndIgnoreReturnsNullMessage() {
        Message result = SelectiveActionBuilder.newRule()
                .on(SelectiveActionBuilder.predicate(true))
                .ignore()
                .process(null);
        assertSame(Message.NULL, result);
    }

    @Test
    public void processWithAllAndIgnoreReturnsNullMessage() {
        Message result = SelectiveActionBuilder.newRule()
                .onOthers()
                .ignore()
                .process(null);
        assertSame(Message.NULL, result);
    }
}
