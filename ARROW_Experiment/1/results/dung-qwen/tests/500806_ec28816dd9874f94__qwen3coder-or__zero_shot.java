package com.consol.citrus.channel.selector;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.xml.xpath.XPathUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.springframework.xml.xpath.XPathParseException;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class XpathPayloadMessageSelector_ArrowTest {

    @Mock
    private TestContext context;

    @Mock
    private Message<?> message;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAcceptWithValidXMLAndMatchingXPath() throws Exception {
        // Setup
        String xmlPayload = "<root><element>testValue</element></root>";
        String xpathExpression = "/root/element";
        String matchingValue = "testValue";

        when(message.getPayload()).thenReturn(xmlPayload);
        when(context.getNamespaceContextBuilder()).thenReturn(mock(com.consol.citrus.context.NamespaceContextBuilder.class));

        // Mock XMLUtils.parseMessagePayload
        Document mockDoc = mock(Document.class);
        when(XMLUtils.parseMessagePayload(anyString())).thenReturn(mockDoc);

        // Mock namespace lookup
        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("default", "http://example.com");
        when(XMLUtils.lookupNamespaces(mockDoc)).thenReturn(namespaces);

        // Mock namespace context builder
        com.consol.citrus.context.NamespaceContextBuilder namespaceBuilder = mock(com.consol.citrus.context.NamespaceContextBuilder.class);
        when(context.getNamespaceContextBuilder()).thenReturn(namespaceBuilder);
        when(namespaceBuilder.getNamespaceMappings()).thenReturn(new HashMap<>());

        // Mock XPath expression creation and evaluation
        org.springframework.xml.xpath.XPathExpression mockExpression = mock(org.springframework.xml.xpath.XPathExpression.class);
        when(XPathExpressionFactory.createXPathExpression(anyString(), anyMap())).thenReturn(mockExpression);
        when(mockExpression.evaluateAsString(mockDoc)).thenReturn(matchingValue);

        // Create selector
        XpathPayloadMessageSelector selector = new XpathPayloadMessageSelector("xpath:" + xpathExpression, matchingValue, context);

        // Execute
        boolean result = selector.accept(message);

        // Verify
        assertTrue(result);
    }

    @Test
    public void testAcceptWithValidXMLAndNonMatchingXPath() throws Exception {
        // Setup
        String xmlPayload = "<root><element>testValue</element></root>";
        String xpathExpression = "/root/element";
        String matchingValue = "differentValue";

        when(message.getPayload()).thenReturn(xmlPayload);
        when(context.getNamespaceContextBuilder()).thenReturn(mock(com.consol.citrus.context.NamespaceContextBuilder.class));

        // Mock XMLUtils.parseMessagePayload
        Document mockDoc = mock(Document.class);
        when(XMLUtils.parseMessagePayload(anyString())).thenReturn(mockDoc);

        // Mock namespace lookup
        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("default", "http://example.com");
        when(XMLUtils.lookupNamespaces(mockDoc)).thenReturn(namespaces);

        // Mock namespace context builder
        com.consol.citrus.context.NamespaceContextBuilder namespaceBuilder = mock(com.consol.citrus.context.NamespaceContextBuilder.class);
        when(context.getNamespaceContextBuilder()).thenReturn(namespaceBuilder);
        when(namespaceBuilder.getNamespaceMappings()).thenReturn(new HashMap<>());

        // Mock XPath expression creation and evaluation
        org.springframework.xml.xpath.XPathExpression mockExpression = mock(org.springframework.xml.xpath.XPathExpression.class);
        when(XPathExpressionFactory.createXPathExpression(anyString(), anyMap())).thenReturn(mockExpression);
        when(mockExpression.evaluateAsString(mockDoc)).thenReturn("testValue");

        // Create selector
        XpathPayloadMessageSelector selector = new XpathPayloadMessageSelector("xpath:" + xpathExpression, matchingValue, context);

        // Execute
        boolean result = selector.accept(message);

        // Verify
        assertFalse(result);
    }

    @Test
    public void testAcceptWithInvalidXML() throws Exception {
        // Setup
        String invalidXmlPayload = "<root><element>testValue</element></root";
        when(message.getPayload()).thenReturn(invalidXmlPayload);

        // Mock XMLUtils.parseMessagePayload to throw LSException
        when(XMLUtils.parseMessagePayload(anyString())).thenThrow(new LSException(LSException.SYNTAX_ERR, "Invalid XML"));

        // Create selector
        XpathPayloadMessageSelector selector = new XpathPayloadMessageSelector("xpath:/root/element", "testValue", context);

        // Execute
        boolean result = selector.accept(message);

        // Verify
        assertFalse(result);
    }

    @Test
    public void testAcceptWithXPathParseException() throws Exception {
        // Setup
        String xmlPayload = "<root><element>testValue</element></root>";
        when(message.getPayload()).thenReturn(xmlPayload);

        // Mock XMLUtils.parseMessagePayload
        Document mockDoc = mock(Document.class);
        when(XMLUtils.parseMessagePayload(anyString())).thenReturn(mockDoc);

        // Mock namespace lookup
        Map<String, String> namespaces = new HashMap<>();
        when(XMLUtils.lookupNamespaces(mockDoc)).thenReturn(namespaces);

        // Mock namespace context builder
        com.consol.citrus.context.NamespaceContextBuilder namespaceBuilder = mock(com.consol.citrus.context.NamespaceContextBuilder.class);
        when(context.getNamespaceContextBuilder()).thenReturn(namespaceBuilder);
        when(namespaceBuilder.getNamespaceMappings()).thenReturn(new HashMap<>());

        // Mock XPath expression creation to throw XPathParseException
        when(XPathExpressionFactory.createXPathExpression(anyString(), anyMap())).thenThrow(new XPathParseException("Invalid XPath"));

        // Create selector
        XpathPayloadMessageSelector selector = new XpathPayloadMessageSelector("xpath:/root/element", "testValue", context);

        // Execute
        boolean result = selector.accept(message);

        // Verify
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
        XpathPayloadMessageSelector selector = factory.create("xpath:/root/element", "value", context);
        assertNotNull(selector);
    }
}
