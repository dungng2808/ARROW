package com.consol.citrus.channel.selector;

import com.consol.citrus.context.TestContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
    public void rejectsNonXmlMessage() {
        TestContext context = new TestContext();
        XpathPayloadMessageSelector selector =
                new XpathPayloadMessageSelector("xpath:/root/value", "expected", context);
        Message<String> message = new GenericMessage<>("not xml");

        assertFalse(selector.accept(message));
    }

    @Test
    public void rejectsMessageForInvalidXPath() {
        TestContext context = new TestContext();
        XpathPayloadMessageSelector selector =
                new XpathPayloadMessageSelector("xpath:[", "expected", context);
        Message<String> message = new GenericMessage<>("<root><value>expected</value></root>");

        assertFalse(selector.accept(message));
    }

    @Test
    public void factorySupportsXPathSelectorKeys() {
        XpathPayloadMessageSelector.Factory factory = new XpathPayloadMessageSelector.Factory();

        assertTrue(factory.supports("xpath:/root/value"));
    }

    @Test
    public void factoryDoesNotSupportOtherKeys() {
        XpathPayloadMessageSelector.Factory factory = new XpathPayloadMessageSelector.Factory();

        assertFalse(factory.supports("header:value"));
    }

    @Test
    public void factoryCreatesSelectorThatAcceptsMatchingMessage() {
        XpathPayloadMessageSelector.Factory factory = new XpathPayloadMessageSelector.Factory();
        XpathPayloadMessageSelector selector = factory.create(
                "xpath:/root/value", "expected", new TestContext());
        Message<String> message = new GenericMessage<>("<root><value>expected</value></root>");

        assertTrue(selector.accept(message));
    }
}
