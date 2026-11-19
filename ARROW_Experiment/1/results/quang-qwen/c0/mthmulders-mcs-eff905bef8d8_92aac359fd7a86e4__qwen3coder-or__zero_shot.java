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
            
            assertEquals("Found @|bold 15|@ results (showing 0)\n", result);
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
            
            assertEquals("Found @|bold 5|@ results\n", result);
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
            var tableConstructor = CommandLine.Help.TextTable.class.getDeclaredConstructor(CommandLine.Help.ColorScheme.class, 
                CommandLine.Help.Column.class, CommandLine.Help.Column.class);
            tableConstructor.setAccessible(true);
            CommandLine.Help.TextTable table = tableConstructor.newInstance(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO),
                new CommandLine.Help.Column(25, 2, CommandLine.Help.Column.Overflow.SPAN),
                new CommandLine.Help.Column(30, 2, CommandLine.Help.Column.Overflow.WRAP));
            
            var method = TabularOutputPrinter.class.getDeclaredMethod("printRow", CommandLine.Help.TextTable.class, SearchResponse.Response.Doc.class);
            method.setAccessible(true);
            method.invoke(printer, table, doc);
            
            String result = baos.toString();
            assertTrue(result.contains("com.example:artifact:1.0.0"));
        } catch (Exception e) {
            fail("Failed to invoke printRow method: " + e.getMessage());
        }
    }

    @Test
    public void testPrint() {
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
        
        String result = baos.toString();
        assertTrue(result.contains("Found @|bold 2|@ results"));
        assertTrue(result.contains("Coordinates"));
        assertTrue(result.contains("Last updated"));
        assertTrue(result.contains("com.example:artifact1:1.0.0"));
        assertTrue(result.contains("com.example:artifact2:2.0.0"));
    }
}
