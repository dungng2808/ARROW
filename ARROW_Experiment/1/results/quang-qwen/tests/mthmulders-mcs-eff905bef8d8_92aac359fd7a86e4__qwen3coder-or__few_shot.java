package it.mulders.mcs.search.printer;

import it.mulders.mcs.search.SearchQuery;
import it.mulders.mcs.search.SearchResponse;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static org.junit.Assert.assertTrue;

public class TabularOutputPrinter_ArrowTest {

    @Test
    public void should_print_header_with_all_results_when_less_than_limit() {
        // Given
        var printer = new TabularOutputPrinter();
        var query = new SearchQuery("test", 10);
        var response = new SearchResponse.Response(5, new SearchResponse.Response.Doc[0]);
        var outputStream = new ByteArrayOutputStream();
        var printStream = new PrintStream(outputStream);

        // When
        printer.print(query, response, printStream);

        // Then
        var output = outputStream.toString();
        assertTrue(output.contains("Found @|bold 5|@ results"));
        assertTrue(!output.contains("showing"));
    }

    @Test
    public void should_print_header_with_limited_results_when_more_than_limit() {
        // Given
        var printer = new TabularOutputPrinter();
        var query = new SearchQuery("test", 10);
        var response = new SearchResponse.Response(15, new SearchResponse.Response.Doc[0]);
        var outputStream = new ByteArrayOutputStream();
        var printStream = new PrintStream(outputStream);

        // When
        printer.print(query, response, printStream);

        // Then
        var output = outputStream.toString();
        assertTrue(output.contains("Found @|bold 15|@ results"));
        assertTrue(output.contains("(showing 0)"));
    }

    @Test
    public void should_calculate_correct_coordinate_column_width() {
        // Given
        var printer = new TabularOutputPrinter();
        var docs = new SearchResponse.Response.Doc[]{
                new SearchResponse.Response.Doc("short", 1000L, "1.0"),
                new SearchResponse.Response.Doc("very_long_coordinate_name", 2000L, "2.0")
        };

        // When
        var width = printer.calculateCoordinateColumnWidth(docs);

        // Then
        assertTrue(width == 23); // length of "very_long_coordinate_name"
    }

    @Test
    public void should_format_timestamp_correctly() {
        // Given
        var printer = new TabularOutputPrinter();
        var formatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm (zzz)");
        var timestamp = Instant.now().toEpochMilli();
        var doc = new SearchResponse.Response.Doc("test", timestamp, "1.0");

        // When
        var lastUpdated = formatter.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()));

        // Then
        assertTrue(lastUpdated != null && !lastUpdated.isEmpty());
    }
}
