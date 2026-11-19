package gr.ekt.bte.record;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import gr.ekt.bte.core.Value;
import gr.ekt.bte.core.StringValue;
import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;

import org.junit.Test;
import static org.junit.Assert.*;

public class XPathRecord_ArrowTest {

    @Test
    public void testConstructor() throws Exception {
        Document doc = createTestDocument();
        Map<String, String> xpathMap = new HashMap<String, String>();
        xpathMap.put("title", "//title");
        xpathMap.put("author", "//author");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        assertNotNull(record);
    }

    @Test
    public void testGetValuesReturnsCorrectValues() throws Exception {
        Document doc = createTestDocument();
        Map<String, String> xpathMap = new HashMap<String, String>();
        xpathMap.put("title", "//title");
        xpathMap.put("author", "//author");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> values = record.getValues("title");

        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals("Test Title", values.get(0).getStringValue());
    }

    @Test
    public void testGetValuesReturnsNullForNonExistentField() throws Exception {
        Document doc = createTestDocument();
        Map<String, String> xpathMap = new HashMap<String, String>();
        xpathMap.put("title", "//title");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> values = record.getValues("nonexistent");

        assertNull(values);
    }

    @Test
    public void testGetValuesHandlesEmptyResults() throws Exception {
        Document doc = createTestDocument();
        Map<String, String> xpathMap = new HashMap<String, String>();
        xpathMap.put("empty", "//nonexistent");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> values = record.getValues("empty");

        assertNotNull(values);
        assertEquals(0, values.size());
    }

    @Test
    public void testGetValuesIgnoresEmptyTextContent() throws Exception {
        Document doc = createDocumentWithEmptyNodes();
        Map<String, String> xpathMap = new HashMap<String, String>();
        xpathMap.put("mixed", "//item");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        List<Value> values = record.getValues("mixed");

        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals("Non-empty content", values.get(0).getStringValue());
    }

    @Test
    public void testMakeMutable() throws Exception {
        Document doc = createTestDocument();
        Map<String, String> xpathMap = new HashMap<String, String>();
        xpathMap.put("title", "//title");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        MutableRecord mutable = record.makeMutable();

        assertNotNull(mutable);
        assertTrue(mutable instanceof MutableRecord);
    }

    @Test
    public void testIsMutable() throws Exception {
        Document doc = createTestDocument();
        Map<String, String> xpathMap = new HashMap<String, String>();
        xpathMap.put("title", "//title");

        XPathRecord record = new XPathRecord(doc, xpathMap);
        assertFalse(record.isMutable());
    }

    private Document createTestDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("root");
        doc.appendChild(root);

        Element title = doc.createElement("title");
        title.setTextContent("Test Title");
        root.appendChild(title);

        Element author = doc.createElement("author");
        author.setTextContent("Test Author");
        root.appendChild(author);

        return doc;
    }

    private Document createDocumentWithEmptyNodes() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("root");
        doc.appendChild(root);

        Element item1 = doc.createElement("item");
        item1.setTextContent("");
        root.appendChild(item1);

        Element item2 = doc.createElement("item");
        item2.setTextContent("Non-empty content");
        root.appendChild(item2);

        return doc;
    }
}
