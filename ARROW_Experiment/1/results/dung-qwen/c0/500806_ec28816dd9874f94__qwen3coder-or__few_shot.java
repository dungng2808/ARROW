package com.consol.citrus.channel.selector;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.xml.xpath.XPathUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.springframework.xml.xpath.XPathParseException;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class XpathPayloadMessageSelector_ArrowTest {

    @Mock
    private TestContext context;

    @Mock
    private Message<?> message;

    @Mock
    private Document document;

    @Mock
    private XPathExpression xPathExpression;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConstructor() {
        String selectKey = "xpath:/root/element";
        String matchingValue = "testValue";
        XpathPayloadMessageSelector selector = new XpathPayloadMessageSelector(selectKey, matchingValue, context);
        assertNotNull(selector);
    }

    @Test
    public void testAcceptWithValidXMLAndMatchingValue() throws Exception {
        String xmlPayload = "<root><element>testValue</element></root>";
        String selectKey = "xpath:/root/element";
        String matchingValue = "testValue";

        when(message.getPayload()).thenReturn(xmlPayload);
        when(XMLUtils.parseMessagePayload(anyString())).thenReturn(document);
        when(XMLUtils.lookupNamespaces(document)).thenReturn(new HashMap<>());
        when(context.getNamespaceContextBuilder()).thenReturn(mock(com.consol.citrus.context.NamespaceContextBuilder.class));
        when(context.getNamespaceContextBuilder().getNamespaceMappings()).thenReturn(new HashMap<>());
        when(XPathExpressionFactory.createXPathExpression(anyString(), anyMap())).thenReturn(xPathExpression);
        when(xPathExpression.evaluateAsString(document)).thenReturn("testValue");

        XpathPayloadMessageSelector selector = new XpathPayloadMessageSelector(selectKey, matchingValue, context);
        boolean result = selector.accept(message);

        assertTrue(result);
    }

    @Test
    public void testAcceptWithValidXMLAndNonMatchingValue() throws Exception {
        String xmlPayload = "<root><element>differentValue</element></root>";
        String selectKey = "xpath:/root/element";
        String matchingValue = "testValue";

        when(message.getPayload()).thenReturn(xmlPayload);
        when(XMLUtils.parseMessagePayload(anyString())).thenReturn(document);
        when(XMLUtils.lookupNamespaces(document)).thenReturn(new HashMap<>());
        when(context.getNamespaceContextBuilder()).thenReturn(mock(com.consol.citrus.context.NamespaceContextBuilder.class));
        when(context.getNamespaceContextBuilder().getNamespaceMappings()).thenReturn(new HashMap<>());
        when(XPathExpressionFactory.createXPathExpression(anyString(), anyMap())).thenReturn(xPathExpression);
        when(xPathExpression.evaluateAsString(document)).thenReturn("differentValue");

        XpathPayloadMessageSelector selector = new XpathPayloadMessageSelector(selectKey, matchingValue, context);
        boolean result = selector.accept(message);

        assertFalse(result);
    }

    @Test
    public void testAcceptWithInvalidXML() throws Exception {
        String xmlPayload = "invalid xml";
        String selectKey = "xpath:/root/element";
        String matchingValue = "testValue";

        when(message.getPayload()).thenReturn(xmlPayload);
        when(XMLUtils.parseMessagePayload(anyString())).thenThrow(new LSException((short) 0, "Invalid XML"));

        XpathPayloadMessageSelector selector = new XpathPayloadMessageSelector(selectKey, matchingValue, context);
        boolean result = selector.accept(message);

        assertFalse(result);
    }

    @Test
    public void testAcceptWithXPathParseException() throws Exception {
        String xmlPayload = "<root><element>testValue</element></root>";
        String selectKey = "xpath:/root/element";
        String matchingValue = "testValue";

        when(message.getPayload()).thenReturn(xmlPayload);
        when(XMLUtils.parseMessagePayload(anyString())).thenReturn(document);
        when(XMLUtils.lookupNamespaces(document)).thenReturn(new HashMap<>());
        when(context.getNamespaceContextBuilder()).thenReturn(mock(com.consol.citrus.context.NamespaceContextBuilder.class));
        when(context.getNamespaceContextBuilder().getNamespaceMappings()).thenReturn(new HashMap<>());
        when(XPathExpressionFactory.createXPathExpression(anyString(), anyMap())).thenThrow(new XPathParseException("Invalid XPath"));

        XpathPayloadMessageSelector selector = new XpathPayloadMessageSelector(selectKey, matchingValue, context);
        boolean result = selector.accept(message);

        assertFalse(result);
    }

    @Test
    public void testFactorySupports() {
        XpathPayloadMessageSelector.Factory factory = new XpathPayloadMessageSelector.Factory();
        assertTrue(factory.supports("xpath:/root/element"));
        assertFalse(factory.supports("other:/root/element"));
    }

    @Test
    public void testFactoryCreate() {
        XpathPayloadMessageSelector.Factory factory = new XpathPayloadMessageSelector.Factory();
        String key = "xpath:/root/element";
        String value = "testValue";
        when(context.getNamespaceContextBuilder()).thenReturn(mock(com.consol.citrus.context.NamespaceContextBuilder.class));
        when(context.getNamespaceContextBuilder().getNamespaceMappings()).thenReturn(new HashMap<>());

        XpathPayloadMessageSelector selector = factory.create(key, value, context);
        assertNotNull(selector);
        assertTrue(selector instanceof XpathPayloadMessageSelector);
    }
}
