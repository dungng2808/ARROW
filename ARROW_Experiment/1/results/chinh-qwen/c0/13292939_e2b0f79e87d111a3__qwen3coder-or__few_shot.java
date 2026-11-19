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
        xpathMap.put("field1", "/root");
        
        XPathRecord record = new XPathRecord(doc, xpathMap);
        assertNotNull(record);
    }

    @Test
    public void testGetValuesReturnsNullWhenFieldNotFound() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root"));
        
        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("field1", "/root");
        
        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> result = record.getValues("nonexistent");
        assertNull(result);
    }

    @Test
    public void testGetValuesWithValidXPath() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root")).appendChild(doc.createElement("child")).setTextContent("value1");
        
        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("field1", "/root/child");
        
        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> result = record.getValues("field1");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("value1", result.get(0).getStringValue());
    }

    @Test
    public void testGetValuesWithEmptyResult() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root"));
        
        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("field1", "/root/nonexistent");
        
        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> result = record.getValues("field1");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetValuesWithEmptyTextContent() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root")).appendChild(doc.createElement("child")).setTextContent("");
        
        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("field1", "/root/child");
        
        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> result = record.getValues("field1");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetValuesWithWhitespaceOnlyTextContent() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root")).appendChild(doc.createElement("child")).setTextContent("   ");
        
        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("field1", "/root/child");
        
        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> result = record.getValues("field1");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testMakeMutable() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root")).appendChild(doc.createElement("child")).setTextContent("value1");
        
        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("field1", "/root/child");
        
        XPathRecord record = new XPathRecord(doc, xpathMap);
        MutableRecord mutableRecord = record.makeMutable();
        assertNotNull(mutableRecord);
    }

    @Test
    public void testIsMutable() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        doc.appendChild(doc.createElement("root"));
        
        Map<String, String> xpathMap = new HashMap<>();
        xpathMap.put("field1", "/root");
        
        XPathRecord record = new XPathRecord(doc, xpathMap);
        assertFalse(record.isMutable());
    }
}
