package it.mulders.mcs.search.printer;

import it.mulders.mcs.search.SearchQuery;
import it.mulders.mcs.search.SearchResponse;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TabularOutputPrinter_ArrowTest {

    @Test
    public void testHeaderWithMoreResultsThanLimit() {
        SearchQuery query = new SearchQuery("test", 10);
        SearchResponse.Response response = new SearchResponse.Response(15, new SearchResponse.Response.Doc[0]);
        
        TabularOutputPrinter printer = new TabularOutputPrinter();
        
        // Using reflection to access private method
        try {
            var method = TabularOutputPrinter.class.getDeclaredMethod("header", SearchQuery.class, SearchResponse.Response.class);
            method.setAccessible(true);
            String result = (String) method.invoke(printer, query, response);
            
            assertEquals("Found @|bold 15|@ results (showing 0)%n", result);
        } catch (Exception e) {
            fail("Failed to invoke header method: " + e.getMessage());
        }
    }

    @Test
    public void testHeaderWithLessResultsThanLimit() {
        SearchQuery query = new SearchQuery("test", 10);
        SearchResponse.Response response = new SearchResponse.Response(5, new SearchResponse.Response.Doc[0]);
        
        TabularOutputPrinter printer = new TabularOutputPrinter();
        
        try {
            var method = TabularOutputPrinter.class.getDeclaredMethod("header", SearchQuery.class, SearchResponse.Response.class);
            method.setAccessible(true);
            String result = (String) method.invoke(printer, query, response);
            
            assertEquals("Found @|bold 5|@ results%n", result);
        } catch (Exception e) {
            fail("Failed to invoke header method: " + e.getMessage());
        }
    }

    @Test
    public void testCalculateCoordinateColumnWidth() {
        SearchResponse.Response.Doc[] docs = {
            new SearchResponse.Response.Doc("com.example:artifact1", 1234567890000L, "1.0.0"),
            new SearchResponse.Response.Doc("com.example:artifact2", 1234567890000L, "2.0.0")
        };
        
        TabularOutputPrinter printer = new TabularOutputPrinter();
        
        try {
            var method = TabularOutputPrinter.class.getDeclaredMethod("calculateCoordinateColumnWidth", SearchResponse.Response.Doc[].class);
            method.setAccessible(true);
            int result = (int) method.invoke(printer, docs);
            
            assertEquals(21, result); // Length of "com.example:artifact2"
        } catch (Exception e) {
            fail("Failed to invoke calculateCoordinateColumnWidth method: " + e.getMessage());
        }
    }

    @Test
    public void testPrintRow() {
        SearchResponse.Response.Doc doc = new SearchResponse.Response.Doc("com.example:artifact", 1234567890000L, "1.0.0");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        TabularOutputPrinter printer = new TabularOutputPrinter();
        
        try {
            var tableField = TabularOutputPrinter.class.getDeclaredField("DATE_TIME_FORMATTER");
            tableField.setAccessible(true);
            DateTimeFormatter formatter = (DateTimeFormatter) tableField.get(printer);
            
            String formattedDate = formatter.format(Instant.ofEpochMilli(doc.timestamp()).atZone(ZoneId.systemDefault()));
            
            // We can't easily test the internal row printing, but we can verify the date formatting works correctly
            assertNotNull(formattedDate);
            assertTrue(formattedDate.contains("2009"));
        } catch (Exception e) {
            fail("Failed to test printRow functionality: " + e.getMessage());
        }
    }

    @Test
    public void testPrintMethod() {
        SearchQuery query = new SearchQuery("test", 10);
        SearchResponse.Response.Doc[] docs = {
            new SearchResponse.Response.Doc("com.example:artifact1", 1234567890000L, "1.0.0"),
            new SearchResponse.Response.Doc("com.example:artifact2", 1234567890000L, "2.0.0")
        };
        SearchResponse.Response response = new SearchResponse.Response(2, docs);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        TabularOutputPrinter printer = new TabularOutputPrinter();
        printer.print(query, response, ps);
        
        String output = baos.toString();
        assertNotNull(output);
        assertTrue(output.contains("Found @|bold 2|@ results"));
        assertTrue(output.contains("Coordinates"));
        assertTrue(output.contains("Last updated"));
        assertTrue(output.contains("com.example:artifact1:1.0.0"));
        assertTrue(output.contains("com.example:artifact2:2.0.0"));
    }

    @Test(expected = IllegalStateException.class)
    public void testCalculateCoordinateColumnWidthEmptyArray() {
        SearchResponse.Response.Doc[] docs = {};
        
        TabularOutputPrinter printer = new TabularOutputPrinter();
        
        try {
            var method = TabularOutputPrinter.class.getDeclaredMethod("calculateCoordinateColumnWidth", SearchResponse.Response.Doc[].class);
            method.setAccessible(true);
            method.invoke(printer, docs);
        } catch (Exception e) {
            fail("Should not throw exception during invocation");
        }
    }
}
