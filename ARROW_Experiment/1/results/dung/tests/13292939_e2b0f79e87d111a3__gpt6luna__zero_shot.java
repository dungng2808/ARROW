package gr.ekt.bte.record;

import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Value;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import static org.junit.Assert.*;

public class XPathRecord_ArrowTest {

    private Document document(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
    }

    @Test
    public void getValuesReturnsTrimmedNonEmptyTextValues() throws Exception {
        Map<String, String> expressions = new HashMap<String, String>();
        expressions.put("title", "/root/title");
        XPathRecord record = new XPathRecord(
                document("<root><title>  Example title  </title><title>   </title><title>Second</title></root>"),
                expressions);

        List<Value> values = record.getValues("title");

        assertEquals(2, values.size());
        assertEquals("Example title", values.get(0).toString());
        assertEquals("Second", values.get(1).toString());
    }

    @Test
    public void getValuesReturnsEmptyListWhenExpressionMatchesNothing() throws Exception {
        Map<String, String> expressions = new HashMap<String, String>();
        expressions.put("missing", "/root/missing");
        XPathRecord record = new XPathRecord(document("<root/>"), expressions);

        List<Value> values = record.getValues("missing");

        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test
    public void getValuesReturnsNullForUnknownField() throws Exception {
        XPathRecord record = new XPathRecord(document("<root/>"), new HashMap<String, String>());

        assertNull(record.getValues("unknown"));
    }

    @Test
    public void isMutableReturnsFalse() throws Exception {
        XPathRecord record = new XPathRecord(document("<root/>"), new HashMap<String, String>());

        assertFalse(record.isMutable());
    }

    @Test
    public void makeMutableReturnsMutableRecord() throws Exception {
        Map<String, String> expressions = new HashMap<String, String>();
        expressions.put("title", "/root/title");
        XPathRecord record = new XPathRecord(document("<root><title>Example</title></root>"), expressions);

        MutableRecord mutableRecord = record.makeMutable();

        assertNotNull(mutableRecord);
        assertTrue(mutableRecord.isMutable());
    }

    @Test(expected = javax.xml.xpath.XPathExpressionException.class)
    public void constructorRejectsInvalidXPath() throws Exception {
        Map<String, String> expressions = new HashMap<String, String>();
        expressions.put("invalid", "//*[");
        new XPathRecord(document("<root/>"), expressions);
    }
}
