package it.mulders.mcs.search.printer;

import it.mulders.mcs.search.SearchQuery;
import it.mulders.mcs.search.SearchResponse;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TabularOutputPrinter_ArrowTest {

    @Test
    public void should_print_header_with_correct_number_of_results() {
        // Given
        var printer = new TabularOutputPrinter();
        var query = mock(SearchQuery.class);
        var response = mock(SearchResponse.Response.class);
        var outputStream = new ByteArrayOutputStream();
        var printStream = new PrintStream(outputStream);

        when(query.searchLimit()).thenReturn(10);
        when(response.numFound()).thenReturn(5);
        when(response.docs()).thenReturn(new SearchResponse.Response.Doc[0]);

        // When
        printer.print(query, response, printStream);

        // Then
        var output = outputStream.toString();
        assertThat(output).contains("Found @|bold 5|@ results");
    }

    @Test
    public void should_print_header_with_showing_message_when_limit_exceeded() {
        // Given
        var printer = new TabularOutputPrinter();
        var query = mock(SearchQuery.class);
        var response = mock(SearchResponse.Response.class);
        var outputStream = new ByteArrayOutputStream();
        var printStream = new PrintStream(outputStream);

        when(query.searchLimit()).thenReturn(10);
        when(response.numFound()).thenReturn(15);
        when(response.docs()).thenReturn(new SearchResponse.Response.Doc[3]);

        // When
        printer.print(query, response, printStream);

        // Then
        var output = outputStream.toString();
        assertThat(output).contains("Found @|bold 15|@ results (showing 3)");
    }

    @Test
    public void should_print_table_with_correct_columns() {
        // Given
        var printer = new TabularOutputPrinter();
        var query = mock(SearchQuery.class);
        var response = mock(SearchResponse.Response.class);
        var outputStream = new ByteArrayOutputStream();
        var printStream = new PrintStream(outputStream);

        when(query.searchLimit()).thenReturn(10);
        when(response.numFound()).thenReturn(1);
        var doc = mock(SearchResponse.Response.Doc.class);
        when(doc.id()).thenReturn("com.example:example-artifact");
        when(doc.latestVersion()).thenReturn("1.0.0");
        when(doc.timestamp()).thenReturn(Instant.now().toEpochMilli());
        when(response.docs()).thenReturn(new SearchResponse.Response.Doc[]{doc});

        // When
        printer.print(query, response, printStream);

        // Then
        var output = outputStream.toString();
        assertThat(output).contains("Coordinates");
        assertThat(output).contains("Last updated");
        assertThat(output).contains("=============");
        assertThat(output).contains("===========");
    }

    @Test
    public void should_print_row_with_formatted_timestamp() {
        // Given
        var printer = new TabularOutputPrinter();
        var query = mock(SearchQuery.class);
        var response = mock(SearchResponse.Response.class);
        var outputStream = new ByteArrayOutputStream();
        var printStream = new PrintStream(outputStream);

        when(query.searchLimit()).thenReturn(10);
        when(response.numFound()).thenReturn(1);
        var doc = mock(SearchResponse.Response.Doc.class);
        when(doc.id()).thenReturn("com.example:example-artifact");
        when(doc.latestVersion()).thenReturn("1.0.0");
        var timestamp = Instant.parse("2023-01-01T12:00:00Z").toEpochMilli();
        when(doc.timestamp()).thenReturn(timestamp);
        when(response.docs()).thenReturn(new SearchResponse.Response.Doc[]{doc});

        // When
        printer.print(query, response, printStream);

        // Then
        var output = outputStream.toString();
        assertThat(output).contains("com.example:example-artifact:1.0.0");
        assertThat(output).contains("01 Jan 2023 at 12:00 (");
    }

    @Test
    public void should_calculate_max_coordinate_length_correctly() {
        // Given
        var printer = new TabularOutputPrinter();
        var doc1 = mock(SearchResponse.Response.Doc.class);
        when(doc1.id()).thenReturn("short");

        var doc2 = mock(SearchResponse.Response.Doc.class);
        when(doc2.id()).thenReturn("very-long-coordinate-name");

        var docs = new SearchResponse.Response.Doc[]{doc1, doc2};

        // When
        var maxLength = printer.calculateCoordinateColumnWidth(docs);

        // Then
        assertThat(maxLength).isEqualTo(25);
    }

    @Test
    public void should_throw_exception_when_no_docs_provided() {
        // Given
        var printer = new TabularOutputPrinter();
        var docs = new SearchResponse.Response.Doc[0];

        // When & Then
        assertThatThrownBy(() -> printer.calculateCoordinateColumnWidth(docs))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Used TabularOutputPrinter without any output");
    }
}
