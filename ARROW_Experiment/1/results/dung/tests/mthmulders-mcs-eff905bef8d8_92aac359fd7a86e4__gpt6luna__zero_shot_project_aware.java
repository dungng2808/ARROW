package it.mulders.mcs.search.printer;

import it.mulders.mcs.search.SearchQuery;
import it.mulders.mcs.search.SearchResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TabularOutputPrinter_ArrowTest {
    @Test
    public void printFormatsHeaderAndDocumentRow() {
        SearchQuery query = new SearchQuery("example", 10);
        SearchResponse.Response.Doc doc = new SearchResponse.Response.Doc("abc", 2, 0L);
        SearchResponse.Response response = new SearchResponse.Response(1, new SearchResponse.Response.Doc[]{doc});
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        new TabularOutputPrinter().print(query, response, stream);

        String timestamp = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm (zzz)")
                .format(Instant.ofEpochMilli(0L).atZone(ZoneId.systemDefault()));
        String expected = "Found 1 results" + System.lineSeparator()
                + "  Coordinates   Last updated" + System.lineSeparator()
                + "  ===========   ============" + System.lineSeparator()
                + "  abc:2         " + timestamp + System.lineSeparator();
        assertEquals(expected, bytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void printShowsNumberOfDisplayedResultsWhenLimitIsExceeded() {
        SearchQuery query = new SearchQuery("example", 1);
        SearchResponse.Response.Doc doc = new SearchResponse.Response.Doc("x", 1, 0L);
        SearchResponse.Response response = new SearchResponse.Response(2, new SearchResponse.Response.Doc[]{doc});
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        new TabularOutputPrinter().print(query, response, stream);

        String output = bytes.toString(StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(output.startsWith("Found 2 (showing 1) results"));
    }

    @Test
    public void printRejectsResponseWithoutDocuments() {
        SearchQuery query = new SearchQuery("example", 10);
        SearchResponse.Response response = new SearchResponse.Response(0, new SearchResponse.Response.Doc[0]);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytes, true, StandardCharsets.UTF_8);

        assertThrows(IllegalStateException.class,
                () -> new TabularOutputPrinter().print(query, response, stream));
    }
}
