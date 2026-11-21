package gr.ekt.bte.record;

import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.Value;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class XPathRecord_ArrowTest {

    @Test
    public void testConstructor() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root"));

        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("title", "/root/title");
        xpathMap.put("author", "/root/author");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        assertNotNull(record);
    }

    @Test
    public void testGetValuesReturnsNullForUnknownField() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root"));

        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("title", "/root/title");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        assertNull(record.getValues("unknown"));
    }

    @Test
    public void testGetValuesWithEmptyResult() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root"));

        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("title", "/root/title");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> values = record.getValues("title");
        assertNotNull(values);
        assertEquals(0, values.size());
    }

    @Test
    public void testGetValuesWithSingleValue() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root")).appendChild(doc.createElement("title")).setTextContent("Sample Title");

        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("title", "/root/title");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> values = record.getValues("title");
        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals("Sample Title", values.get(0).getStringValue());
    }

    @Test
    public void testGetValuesWithMultipleValues() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root"))
                .appendChild(doc.createElement("item")).setTextContent("Item 1");
        doc.getDocumentElement().getLastChild()
                .appendChild(doc.createElement("item")).setTextContent("Item 2");

        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("item", "/root/item");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> values = record.getValues("item");
        assertNotNull(values);
        assertEquals(2, values.size());
        assertEquals("Item 1", values.get(0).getStringValue());
        assertEquals("Item 2", values.get(1).getStringValue());
    }

    @Test
    public void testGetValuesWithEmptyTextContent() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root"))
                .appendChild(doc.createElement("title")).setTextContent("   ");

        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("title", "/root/title");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> values = record.getValues("title");
        assertNotNull(values);
        assertEquals(0, values.size());
    }

    @Test
    public void testMakeMutable() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root"))
                .appendChild(doc.createElement("title")).setTextContent("Sample Title");

        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("title", "/root/title");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        MutableRecord mutableRecord = record.makeMutable();
        assertNotNull(mutableRecord);
        assertTrue(mutableRecord instanceof MutableRecord);
    }

    @Test
    public void testIsMutable() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root"));

        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("title", "/root/title");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        assertFalse(record.isMutable());
    }
}
