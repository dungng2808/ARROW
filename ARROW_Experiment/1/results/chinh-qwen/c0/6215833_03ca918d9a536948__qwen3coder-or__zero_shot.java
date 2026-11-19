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
import java.util.*;

import static org.junit.Assert.*;

public class FetchTableRowItem_ArrowTest {

    @Test
    public void testConstructorWithValidFile() throws IOException {
        // Create a temporary CSV file
        File tempFile = File.createTempFile("test", ".csv");
        PrintWriter writer = new PrintWriter(tempFile);
        writer.println("col1,col2,col3");
        writer.println("val1,val2,val3");
        writer.close();

        // Create FetchTableRowItem instance
        FetchTableRowItem item = new FetchTableRowItem(tempFile.getParent() + File.separator, "test");

        // Verify table name
        assertEquals("test", item.getTableName());

        // Verify table columns
        Table table = item.getTable();
        assertNotNull(table);
        assertEquals("test", table.getName());
        assertEquals(3, table.getColumns().size());
        assertEquals("col1", table.getColumns().get(0).getName());
        assertEquals("col2", table.getColumns().get(1).getName());
        assertEquals("col3", table.getColumns().get(2).getName());

        // Clean up
        tempFile.delete();
    }

    @Test
    public void testFetchResultRecordSetWithValidData() throws IOException {
        // Create a temporary CSV file
        File tempFile = File.createTempFile("test", ".csv");
        PrintWriter writer = new PrintWriter(tempFile);
        writer.println("col1,col2,col3");
        writer.println("val1,val2,val3");
        writer.println("val4,val5,val6");
        writer.close();

        // Create FetchTableRowItem instance
        FetchTableRowItem item = new FetchTableRowItem(tempFile.getParent() + File.separator, "test");

        // First call should return the first row
        RecordSet rs1 = item.fetchResultRecordSet();
        assertNotNull(rs1);
        assertEquals(3, rs1.getColumns().size());
        assertEquals(1, rs1.getRows().size());
        assertEquals("val1", rs1.getRows().get(0).getValues().get(0));
        assertEquals("val2", rs1.getRows().get(0).getValues().get(1));
        assertEquals("val3", rs1.getRows().get(0).getValues().get(2));

        // Second call should return the second row
        RecordSet rs2 = item.fetchResultRecordSet();
        assertNotNull(rs2);
        assertEquals(3, rs2.getColumns().size());
        assertEquals(1, rs2.getRows().size());
        assertEquals("val4", rs2.getRows().get(0).getValues().get(0));
        assertEquals("val5", rs2.getRows().get(0).getValues().get(1));
        assertEquals("val6", rs2.getRows().get(0).getValues().get(2));

        // Third call should return null since there are no more rows
        RecordSet rs3 = item.fetchResultRecordSet();
        assertNull(rs3);

        // Clean up
        tempFile.delete();
    }

    @Test
    public void testFetchResultRecordSetWithEmptyFile() throws IOException {
        // Create an empty CSV file
        File tempFile = File.createTempFile("test", ".csv");
        PrintWriter writer = new PrintWriter(tempFile);
        writer.println("col1,col2,col3");
        writer.close();

        // Create FetchTableRowItem instance
        FetchTableRowItem item = new FetchTableRowItem(tempFile.getParent() + File.separator, "test");

        // Call fetchResultRecordSet - should return null because there are no data rows
        RecordSet rs = item.fetchResultRecordSet();
        assertNull(rs);

        // Clean up
        tempFile.delete();
    }

    @Test
    public void testGetTable() {
        FetchTableRowItem item = new FetchTableRowItem("", "test");
        Table table = item.getTable();
        assertNotNull(table);
        assertEquals("test", table.getName());
    }

    @Test
    public void testSetTable() {
        FetchTableRowItem item = new FetchTableRowItem("", "test");
        Table newTable = new Table();
        newTable.setName("newTable");
        item.setTable(newTable);
        assertEquals("newTable", item.getTable().getName());
    }

    @Test
    public void testGetTableName() {
        FetchTableRowItem item = new FetchTableRowItem("", "test");
        assertEquals("test", item.getTableName());
    }

    @Test
    public void testSetTableName() {
        FetchTableRowItem item = new FetchTableRowItem("", "test");
        item.setTableName("newName");
        assertEquals("newName", item.getTableName());
    }

    @Test
    public void testGetListReader() {
        FetchTableRowItem item = new FetchTableRowItem("", "test");
        assertNotNull(item.getListReader());
    }

    @Test
    public void testSetListReader() {
        FetchTableRowItem item = new FetchTableRowItem("", "test");
        ICsvListReader mockReader = new CsvListReader(new StringReader(""), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
        item.setListReader(mockReader);
        assertEquals(mockReader, item.getListReader());
    }
}
