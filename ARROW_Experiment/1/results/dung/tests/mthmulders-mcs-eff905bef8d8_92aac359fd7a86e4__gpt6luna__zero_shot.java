package it.mulders.mcs.search.printer;

import it.mulders.mcs.search.SearchQuery;
import it.mulders.mcs.search.SearchResponse;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

public class TabularOutputPrinter_ArrowTest {
    @Test
    public void printIncludesHeaderAndDocumentValues() throws Exception {
        SearchQuery query = new SearchQuery();
        SearchResponse.Response.Doc doc = new SearchResponse.Response.Doc(
                "abc", "1.0", 0L
        );
        SearchResponse.Response response = new SearchResponse.Response(
                1, new SearchResponse.Response.Doc[]{doc}
        );
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytes, true, StandardCharsets.UTF_8.name());

        new TabularOutputPrinter().print(query, response, stream);

        String output = bytes.toString(StandardCharsets.UTF_8.name());
        assertTrue(output.contains("Found"));
        assertTrue(output.contains("1"));
        assertTrue(output.contains("Coordinates"));
        assertTrue(output.contains("Last updated"));
        assertTrue(output.contains("abc:1.0"));
    }
}
