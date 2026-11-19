package com.consol.citrus.channel.selector;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.xml.xpath.XPathUtils;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.springframework.xml.xpath.XPathParseException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSException;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class XpathPayloadMessageSelector_ArrowTest {

    private TestContext context;
    private Message<?> message;
    private Document document;
    private XpathPayloadMessageSelector selector;

    @BeforeMethod
    public void setUp() {
        context = Mockito.mock(TestContext.class);
        message = Mockito.mock(Message.class);
        document = Mockito.mock(Document.class);
        selector = new XpathPayloadMessageSelector("xpath:/root/element", "expectedValue", context);
    }

    @Test
    public void testAcceptWithValidXMLAndMatchingValue() throws Exception {
        // Setup
        when(message.getPayload()).thenReturn("<root><element>expectedValue</element></root>");
        when(XMLUtils.parseMessagePayload(anyString())).thenReturn(document);
        when(XMLUtils.lookupNamespaces(document)).thenReturn(new HashMap<>());
        when(context.getNamespaceContextBuilder()).thenReturn(Mockito.mock(com.consol.citrus.context.NamespaceContextBuilder.class));
        when(context.getNamespaceContextBuilder().getNamespaceMappings()).thenReturn(new HashMap<>());
        
        // Mock XPath expression evaluation to return matching value
        org.springframework.xml.xpath.XPathExpression expression = Mockito.mock(org.springframework.xml.xpath.XPathExpression.class);
        when(XPathExpressionFactory.createXPathExpression(anyString(), any(Map.class))).thenReturn(expression);
        when(expression.evaluateAsString(document)).thenReturn("expectedValue");

        // Execute
        boolean result = selector.accept(message);

        // Verify
        assert result == true;
    }

    @Test
    public void testAcceptWithValidXMLAndNonMatchingValue() throws Exception {
        // Setup
        when(message.getPayload()).thenReturn("<root><element>differentValue</element></root>");
        when(XMLUtils.parseMessagePayload(anyString())).thenReturn(document);
        when(XMLUtils.lookupNamespaces(document)).thenReturn(new HashMap<>());
        when(context.getNamespaceContextBuilder()).thenReturn(Mockito.mock(com.consol.citrus.context.NamespaceContextBuilder.class));
        when(context.getNamespaceContextBuilder().getNamespaceMappings()).thenReturn(new HashMap<>());
        
        // Mock XPath expression evaluation to return non-matching value
        org.springframework.xml.xpath.XPathExpression expression = Mockito.mock(org.springframework.xml.xpath.XPathExpression.class);
        when(XPathExpressionFactory.createXPathExpression(anyString(), any(Map.class))).thenReturn(expression);
        when(expression.evaluateAsString(document)).thenReturn("differentValue");

        // Execute
        boolean result = selector.accept(message);

        // Verify
        assert result == false;
    }

    @Test
    public void testAcceptWithInvalidXML() throws Exception {
        // Setup
        when(message.getPayload()).thenReturn("invalid xml");
        when(XMLUtils.parseMessagePayload(anyString())).thenThrow(new LSException((short) 0, "Invalid XML"));

        // Execute
        boolean result = selector.accept(message);

        // Verify
        assert result == false;
    }

    @Test
    public void testAcceptWithXPathParseException() throws Exception {
        // Setup
        when(message.getPayload()).thenReturn("<root><element>value</element></root>");
        when(XMLUtils.parseMessagePayload(anyString())).thenReturn(document);
        when(XMLUtils.lookupNamespaces(document)).thenReturn(new HashMap<>());
        when(context.getNamespaceContextBuilder()).thenReturn(Mockito.mock(com.consol.citrus.context.NamespaceContextBuilder.class));
        when(context.getNamespaceContextBuilder().getNamespaceMappings()).thenReturn(new HashMap<>());
        
        // Mock XPath expression creation to throw exception
        when(XPathExpressionFactory.createXPathExpression(anyString(), any(Map.class))).thenThrow(new XPathParseException("Invalid XPath"));

        // Execute
        boolean result = selector.accept(message);

        // Verify
        assert result == false;
    }

    @Test
    public void testFactorySupports() {
        XpathPayloadMessageSelector.Factory factory = new XpathPayloadMessageSelector.Factory();
        assert factory.supports("xpath:/root/element") == true;
        assert factory.supports("http://example.com") == false;
    }

    @Test
    public void testFactoryCreate() {
        XpathPayloadMessageSelector.Factory factory = new XpathPayloadMessageSelector.Factory();
        XpathPayloadMessageSelector created = factory.create("xpath:/root/element", "value", context);
        assert created != null;
        assert created.getClass() == XpathPayloadMessageSelector.class;
    }
}
