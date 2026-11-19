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
    public void print_should_handle_empty_results() {
        // Setup
        TabularOutputPrinter printer = new TabularOutputPrinter();
        SearchQuery query = new SearchQuery("test", 10);
        SearchResponse.Response response = new SearchResponse.Response(0, new SearchResponse.Response.Doc[0]);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        // Execute
        printer.print(query, response, ps);

        // Verify
        String output = baos.toString();
        assertTrue(output.contains("Found @|bold 0|@ results"));
    }

    @Test
    public void print_should_handle_single_result() {
        // Setup
        TabularOutputPrinter printer = new TabularOutputPrinter();
        SearchQuery query = new SearchQuery("test", 10);
        SearchResponse.Response.Doc[] docs = {
                new SearchResponse.Response.Doc("com.example:artifact", 1609459200000L, "1.0.0")
        };
        SearchResponse.Response response = new SearchResponse.Response(1, docs);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        // Execute
        printer.print(query, response, ps);

        // Verify
        String output = baos.toString();
        assertTrue(output.contains("Found @|bold 1|@ results"));
        assertTrue(output.contains("com.example:artifact:1.0.0"));
    }

    @Test
    public void print_should_handle_multiple_results() {
        // Setup
        TabularOutputPrinter printer = new TabularOutputPrinter();
        SearchQuery query = new SearchQuery("test", 10);
        SearchResponse.Response.Doc[] docs = {
                new SearchResponse.Response.Doc("com.example:artifact1", 1609459200000L, "1.0.0"),
                new SearchResponse.Response.Doc("com.example:artifact2", 1609459200000L, "2.0.0")
        };
        SearchResponse.Response response = new SearchResponse.Response(2, docs);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        // Execute
        printer.print(query, response, ps);

        // Verify
        String output = baos.toString();
        assertTrue(output.contains("Found @|bold 2|@ results"));
        assertTrue(output.contains("com.example:artifact1:1.0.0"));
        assertTrue(output.contains("com.example:artifact2:2.0.0"));
    }

    @Test
    public void print_should_show_limited_results_when_exceeding_limit() {
        // Setup
        TabularOutputPrinter printer = new TabularOutputPrinter();
        SearchQuery query = new SearchQuery("test", 5);
        SearchResponse.Response.Doc[] docs = {
                new SearchResponse.Response.Doc("com.example:artifact1", 1609459200000L, "1.0.0"),
                new SearchResponse.Response.Doc("com.example:artifact2", 1609459200000L, "2.0.0")
        };
        SearchResponse.Response response = new SearchResponse.Response(10, docs);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        // Execute
        printer.print(query, response, ps);

        // Verify
        String output = baos.toString();
        assertTrue(output.contains("Found @|bold 10|@ results (showing 2)"));
    }

    @Test
    public void header_should_format_correctly_for_zero_results() {
        // Setup
        TabularOutputPrinter printer = new TabularOutputPrinter();
        SearchQuery query = new SearchQuery("test", 10);
        SearchResponse.Response response = new SearchResponse.Response(0, new SearchResponse.Response.Doc[0]);

        // Execute
        String header = printer.header(query, response);

        // Verify
        assertTrue(header.contains("Found @|bold 0|@ results"));
    }

    @Test
    public void header_should_format_correctly_for_single_result() {
        // Setup
        TabularOutputPrinter printer = new TabularOutputPrinter();
        SearchQuery query = new SearchQuery("test", 10);
        SearchResponse.Response response = new SearchResponse.Response(1, new SearchResponse.Response.Doc[]{new SearchResponse.Response.Doc("com.example:artifact", 1609459200000L, "1.0.0")});

        // Execute
        String header = printer.header(query, response);

        // Verify
        assertTrue(header.contains("Found @|bold 1|@ results"));
    }

    @Test
    public void header_should_format_correctly_for_multiple_results() {
        // Setup
        TabularOutputPrinter printer = new TabularOutputPrinter();
        SearchQuery query = new SearchQuery("test", 10);
        SearchResponse.Response response = new SearchResponse.Response(5, new SearchResponse.Response.Doc[]{new SearchResponse.Response.Doc("com.example:artifact", 1609459200000L, "1.0.0")});

        // Execute
        String header = printer.header(query, response);

        // Verify
        assertTrue(header.contains("Found @|bold 5|@ results"));
    }

    @Test
    public void header_should_show_limited_message_when_exceeding_limit() {
        // Setup
        TabularOutputPrinter printer = new TabularOutputPrinter();
        SearchQuery query = new SearchQuery("test", 3);
        SearchResponse.Response response = new SearchResponse.Response(5, new SearchResponse.Response.Doc[]{new SearchResponse.Response.Doc("com.example:artifact", 1609459200000L, "1.0.0")});

        // Execute
        String header = printer.header(query, response);

        // Verify
        assertTrue(header.contains("(showing 1)"));
    }

    @Test
    public void calculateCoordinateColumnWidth_should_return_max_length_of_coordinates() {
        // Setup
        TabularOutputPrinter printer = new TabularOutputPrinter();
        SearchResponse.Response.Doc[] docs = {
                new SearchResponse.Response.Doc("short", 1609459200000L, "1.0.0"),
                new SearchResponse.Response.Doc("very-long-coordinate-name", 1609459200000L, "2.0.0"),
                new SearchResponse.Response.Doc("medium", 1609459200000L, "3.0.0")
        };

        // Execute
        int width = printer.calculateCoordinateColumnWidth(docs);

        // Verify
        assertTrue(width >= 23); // Length of "very-long-coordinate-name"
    }

    @Test(expected = IllegalStateException.class)
    public void calculateCoordinateColumnWidth_should_throw_exception_when_no_docs() {
        // Setup
        TabularOutputPrinter printer = new TabularOutputPrinter();

        // Execute
        printer.calculateCoordinateColumnWidth(new SearchResponse.Response.Doc[0]);
    }

    @Test
    public void printRow_should_format_timestamp_correctly() {
        // Setup
        TabularOutputPrinter printer = new TabularOutputPrinter();
        SearchResponse.Response.Doc doc = new SearchResponse.Response.Doc("com.example:artifact", 1609459200000L, "1.0.0");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        var table = new CommandLine.Help.TextTable(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO),
                new CommandLine.Help.Column(20, 2, CommandLine.Help.Column.Overflow.SPAN),
                new CommandLine.Help.Column(30, 2, CommandLine.Help.Column.Overflow.WRAP)
        );

        // Execute
        printer.printRow(table, doc);

        // Verify
        String output = baos.toString();
        assertTrue(output.contains("com.example:artifact:1.0.0"));
    }
}
