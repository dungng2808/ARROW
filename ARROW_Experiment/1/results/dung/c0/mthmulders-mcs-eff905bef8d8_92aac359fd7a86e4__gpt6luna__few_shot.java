package it.mulders.mcs.search.printer;

import it.mulders.mcs.search.SearchQuery;
import it.mulders.mcs.search.SearchResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TabularOutputPrinter_ArrowTest {

    @Test
    public void printIncludesResultCountCoordinatesAndFormattedTimestamp() throws Exception {
        SearchQuery query = new SearchQuery("example", 10);
        SearchResponse.Response.Doc doc =
                new SearchResponse.Response.Doc("abc", 2, 0L);
        SearchResponse.Response response =
                new SearchResponse.Response(1, new SearchResponse.Response.Doc[] {doc});
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytes, true, "UTF-8");

        new TabularOutputPrinter().print(query, response, stream);

        String output = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        String expectedTimestamp = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm (zzz)")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(0L));
        assertTrue(output.contains("Found 1 results"));
        assertTrue(output.contains("Coordinates"));
        assertTrue(output.contains("abc:2"));
        assertTrue(output.contains(expectedTimestamp));
    }

    @Test
    public void printShowsNumberOfDocumentsWhenResultsExceedLimit() throws Exception {
        SearchQuery query = new SearchQuery("example", 1);
        SearchResponse.Response.Doc doc =
                new SearchResponse.Response.Doc("x", 1, 0L);
        SearchResponse.Response response =
                new SearchResponse.Response(2, new SearchResponse.Response.Doc[] {doc});
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytes, true, "UTF-8");

        new TabularOutputPrinter().print(query, response, stream);

        String output = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("Found 2 results (showing 1)"));
    }
}
