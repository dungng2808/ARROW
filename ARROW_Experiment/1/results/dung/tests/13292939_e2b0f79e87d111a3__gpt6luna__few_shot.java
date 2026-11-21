package gr.ekt.bte.record;

import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.Value;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class XPathRecord_ArrowTest {

    private Document document(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    @Test
    public void getValuesReturnsTrimmedNonEmptyText() throws Exception {
        Map<String, String> expressions = new HashMap<String, String>();
        expressions.put("title", "/root/title");
        XPathRecord record = new XPathRecord(
                document("<root><title>  Example title  </title><title>   </title><title>Second</title></root>"),
                expressions);

        List<Value> values = record.getValues("title");

        List<Value> expected = new ArrayList<Value>();
        expected.add(new gr.ekt.bte.core.StringValue("Example title"));
        expected.add(new gr.ekt.bte.core.StringValue("Second"));
        assertEquals(expected, values);
    }

    @Test
    public void getValuesReturnsEmptyListWhenNoNodesMatch() throws Exception {
        Map<String, String> expressions = new HashMap<String, String>();
        expressions.put("missing", "/root/missing");
        XPathRecord record = new XPathRecord(document("<root><title>Example</title></root>"), expressions);

        assertEquals(0, record.getValues("missing").size());
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
    public void makeMutableCopiesConfiguredFields() throws Exception {
        Map<String, String> expressions = new HashMap<String, String>();
        expressions.put("name", "/root/name");
        XPathRecord record = new XPathRecord(document("<root><name>Ada</name></root>"), expressions);

        MutableRecord mutable = record.makeMutable();

        assertEquals(true, mutable.isMutable());
        assertEquals(record.getValues("name"), mutable.getValues("name"));
    }
}
