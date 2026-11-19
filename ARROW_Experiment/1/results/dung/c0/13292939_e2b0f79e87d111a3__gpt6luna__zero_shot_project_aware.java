package gr.ekt.bte.record;

import gr.ekt.bte.core.MutableRecord;
import gr.ekt.bte.core.Value;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class XPathRecord_ArrowTest {

    private Document documentWithItems(String... values) throws Exception {
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element root = document.createElement("root");
        document.appendChild(root);
        for (String value : values) {
            Element item = document.createElement("item");
            item.appendChild(document.createTextNode(value));
            root.appendChild(item);
        }
        return document;
    }

    @Test
    public void getValuesReturnsTrimmedNonEmptyTextInDocumentOrder() throws Exception {
        Document document = documentWithItems("  first  ", "   ", "second");
        Map<String, String> expressions = new HashMap<String, String>();
        expressions.put("items", "/root/item");

        XPathRecord record = new XPathRecord(document, expressions);
        List<Value> values = record.getValues("items");

        assertEquals(2, values.size());
        assertEquals("first", values.get(0).toString());
        assertEquals("second", values.get(1).toString());
    }

    @Test
    public void getValuesReturnsEmptyListWhenExpressionMatchesNothing() throws Exception {
        Document document = documentWithItems("value");
        Map<String, String> expressions = new HashMap<String, String>();
        expressions.put("missing", "/root/absent");

        XPathRecord record = new XPathRecord(document, expressions);

        assertEquals(0, record.getValues("missing").size());
    }

    @Test
    public void getValuesReturnsNullForUnknownField() throws Exception {
        XPathRecord record = new XPathRecord(documentWithItems(),
                new HashMap<String, String>());

        assertNull(record.getValues("unknown"));
    }

    @Test
    public void recordIsNotMutable() throws Exception {
        XPathRecord record = new XPathRecord(documentWithItems(),
                new HashMap<String, String>());

        assertFalse(record.isMutable());
    }

    @Test
    public void makeMutableCopiesConfiguredFieldsAndValues() throws Exception {
        Document document = documentWithItems("  alpha ", "beta");
        Map<String, String> expressions = new HashMap<String, String>();
        expressions.put("items", "/root/item");

        XPathRecord record = new XPathRecord(document, expressions);
        MutableRecord mutable = record.makeMutable();

        assertTrue(mutable.isMutable());
        List<Value> values = mutable.getValues("items");
        assertEquals(2, values.size());
        assertEquals("alpha", values.get(0).toString());
        assertEquals("beta", values.get(1).toString());
    }
}
