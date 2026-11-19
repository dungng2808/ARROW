package com.automationrockstars.bmo.event.processor;

import com.automationrockstars.gunter.EventType;
import com.automationrockstars.gunter.events.Event;
import com.automationrockstars.gunter.events.impl.EventImplUtils;
import com.google.common.base.Predicate;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class SelectiveActionBuilder_ArrowTest {

    @Test
    public void testNewRule() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        assertNotNull(builder);
    }

    @Test
    public void testOnClass() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        SelectiveActionBuilder result = builder.on(Event.class);
        assertEquals(builder, result);
    }

    @Test
    public void testOnOthers() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        SelectiveActionBuilder result = builder.onOthers();
        assertEquals(builder, result);
    }

    @Test
    public void testIgnore() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        SelectiveActionBuilder result = builder.ignore();
        assertEquals(builder, result);
    }

    @Test
    public void testStore() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        SelectiveActionBuilder result = builder.store();
        assertEquals(builder, result);
    }

    @Test
    public void testPassTrough() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        SelectiveActionBuilder result = builder.passTrough();
        assertEquals(builder, result);
    }

    @Test
    public void testOnEventType() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        SelectiveActionBuilder result = builder.on(EventType.ALL);
        assertEquals(builder, result);
    }

    @Test
    public void testOnPredicate() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        Predicate<Event> predicate = new Predicate<Event>() {
            @Override
            public boolean apply(Event input) {
                return true;
            }
        };
        SelectiveActionBuilder result = builder.on(predicate);
        assertEquals(builder, result);
    }

    @Test
    public void testRun() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        SelectiveActionBuilder result = builder.run(Action.DO_NOTHING);
        assertEquals(builder, result);
    }

    @Test
    public void testProcess() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        builder.on(Event.class).run(Action.DO_NOTHING);
        Event event = EventImplUtils.createEvent(EventType.ALL);
        Message result = builder.process(event);
        assertNotNull(result);
    }

    @Test
    public void testProcessWithNullResult() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        builder.on(Event.class).run(Action.STORE);
        Event event = EventImplUtils.createEvent(EventType.ALL);
        Message result = builder.process(event);
        assertNotNull(result);
    }

    @Test
    public void testProcessNoMatchingRule() {
        SelectiveActionBuilder builder = SelectiveActionBuilder.newRule();
        builder.on(Event.class).run(Action.DO_NOTHING);
        Event event = EventImplUtils.createEvent(EventType.ALL);
        Message result = builder.process(event);
        assertNotNull(result);
    }

    @Test
    public void testCreateClassPredicate() {
        Predicate<Event> predicate = SelectiveActionBuilder.createClassPredicate(Event.class);
        assertNotNull(predicate);
    }

    @Test
    public void testPredicate() {
        Predicate<Event> predicate = SelectiveActionBuilder.predicate(true);
        assertNotNull(predicate);
    }
}
