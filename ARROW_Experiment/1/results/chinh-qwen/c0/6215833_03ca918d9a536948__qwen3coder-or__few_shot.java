package lrn.plan;

import lrn.recordset.Column;
import lrn.recordset.RecordSet;
import lrn.recordset.Row;
import lrn.table.Table;
import org.junit.Test;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class FetchTableRowItem_ArrowTest {

    @Test
    public void testConstructor() throws Exception {
        // Create a temporary CSV file for testing
        File tempFile = File.createTempFile("test", ".csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write("col1,col2,col3");
        writer.newLine();
        writer.close();

        FetchTableRowItem fetchItem = new FetchTableRowItem(tempFile.getParent() + File.separator, "test");

        assertNotNull(fetchItem.getTable());
        assertEquals("test", fetchItem.getTableName());
        assertNotNull(fetchItem.getListReader());

        // Clean up
        tempFile.delete();
    }

    @Test
    public void testFetchResultRecordSet() throws Exception {
        // Create a temporary CSV file for testing
        File tempFile = File.createTempFile("test", ".csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write("col1,col2,col3");
        writer.newLine();
        writer.write("val1,val2,val3");
        writer.newLine();
        writer.close();

        FetchTableRowItem fetchItem = new FetchTableRowItem(tempFile.getParent() + File.separator, "test");

        RecordSet result = fetchItem.fetchResultRecordSet();

        assertNotNull(result);
        assertEquals(1, result.getRows().size());
        assertEquals(3, result.getColumns().size());

        // Clean up
        tempFile.delete();
    }

    @Test
    public void testFetchResultRecordSetEmpty() throws Exception {
        // Create a temporary CSV file for testing
        File tempFile = File.createTempFile("test", ".csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write("col1,col2,col3");
        writer.newLine();
        writer.close();

        FetchTableRowItem fetchItem = new FetchTableRowItem(tempFile.getParent() + File.separator, "test");

        RecordSet result = fetchItem.fetchResultRecordSet();

        assertNull(result);

        // Clean up
        tempFile.delete();
    }

    @Test
    public void testGetTable() throws Exception {
        // Create a temporary CSV file for testing
        File tempFile = File.createTempFile("test", ".csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write("col1,col2,col3");
        writer.newLine();
        writer.close();

        FetchTableRowItem fetchItem = new FetchTableRowItem(tempFile.getParent() + File.separator, "test");

        Table table = fetchItem.getTable();
        assertNotNull(table);
        assertEquals("test", table.getName());

        // Clean up
        tempFile.delete();
    }

    @Test
    public void testGetTableName() throws Exception {
        // Create a temporary CSV file for testing
        File tempFile = File.createTempFile("test", ".csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write("col1,col2,col3");
        writer.newLine();
        writer.close();

        FetchTableRowItem fetchItem = new FetchTableRowItem(tempFile.getParent() + File.separator, "test");

        assertEquals("test", fetchItem.getTableName());

        // Clean up
        tempFile.delete();
    }

    @Test
    public void testSetTable() throws Exception {
        // Create a temporary CSV file for testing
        File tempFile = File.createTempFile("test", ".csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write("col1,col2,col3");
        writer.newLine();
        writer.close();

        FetchTableRowItem fetchItem = new FetchTableRowItem(tempFile.getParent() + File.separator, "test");

        Table newTable = new Table();
        newTable.setName("newTable");
        fetchItem.setTable(newTable);

        assertEquals("newTable", fetchItem.getTable().getName());

        // Clean up
        tempFile.delete();
    }

    @Test
    public void testSetTableName() throws Exception {
        // Create a temporary CSV file for testing
        File tempFile = File.createTempFile("test", ".csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write("col1,col2,col3");
        writer.newLine();
        writer.close();

        FetchTableRowItem fetchItem = new FetchTableRowItem(tempFile.getParent() + File.separator, "test");

        fetchItem.setTableName("newTableName");
        assertEquals("newTableName", fetchItem.getTableName());

        // Clean up
        tempFile.delete();
    }
}
