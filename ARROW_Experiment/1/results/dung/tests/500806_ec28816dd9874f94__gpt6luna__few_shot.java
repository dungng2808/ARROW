package com.consol.citrus.channel.selector;

import com.consol.citrus.context.TestContext;
import org.junit.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class XpathPayloadMessageSelector_ArrowTest {

    @Test
    public void acceptsMessageWhenXPathValueMatches() {
        TestContext context = new TestContext();
        XpathPayloadMessageSelector selector =
                new XpathPayloadMessageSelector("xpath:/root/value", "expected", context);
        Message<String> message = new GenericMessage<>("<root><value>expected</value></root>");

        assertTrue(selector.accept(message));
    }

    @Test
    public void rejectsMessageWhenXPathValueDoesNotMatch() {
        TestContext context = new TestContext();
        XpathPayloadMessageSelector selector =
                new XpathPayloadMessageSelector("xpath:/root/value", "expected", context);
        Message<String> message = new GenericMessage<>("<root><value>other</value></root>");

        assertFalse(selector.accept(message));
    }

    @Test
    public void rejectsMessageWhenXPathIsInvalid() {
        TestContext context = new TestContext();
        XpathPayloadMessageSelector selector =
                new XpathPayloadMessageSelector("xpath:/root[", "expected", context);
        Message<String> message = new GenericMessage<>("<root><value>expected</value></root>");

        assertFalse(selector.accept(message));
    }

    @Test
    public void factorySupportsXPathPrefix() {
        XpathPayloadMessageSelector.Factory factory = new XpathPayloadMessageSelector.Factory();

        assertTrue(factory.supports("xpath:/root/value"));
    }

    @Test
    public void factoryDoesNotSupportOtherPrefix() {
        XpathPayloadMessageSelector.Factory factory = new XpathPayloadMessageSelector.Factory();

        assertFalse(factory.supports("header:value"));
    }

    @Test
    public void factoryCreatesSelectorThatAcceptsMatchingMessage() {
        XpathPayloadMessageSelector.Factory factory = new XpathPayloadMessageSelector.Factory();
        TestContext context = new TestContext();
        XpathPayloadMessageSelector selector =
                factory.create("xpath:/root/value", "expected", context);
        Message<String> message = new GenericMessage<>("<root><value>expected</value></root>");

        assertTrue(selector.accept(message));
    }
}
