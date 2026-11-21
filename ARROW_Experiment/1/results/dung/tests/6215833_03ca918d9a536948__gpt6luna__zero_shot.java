package lrn.plan;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lrn.recordset.RecordSet;
import lrn.table.Table;
import org.junit.Test;
import static org.junit.Assert.*;

public class FetchTableRowItem_ArrowTest {

    @Test
    public void constructorReadsTableNameAndColumnHeaders() throws IOException {
        File directory = createDirectory();
        writeCsv(directory, "people", "name;age\nAlice;30\n");

        FetchTableRowItem item = new FetchTableRowItem(directory.getAbsolutePath() + File.separator, "people");

        assertEquals("people", item.getTableName());
        assertEquals("people", item.getTable().getName());
        assertEquals(2, item.getTable().getColumns().size());
        assertEquals("name", item.getTable().getColumns().get(0).getName());
        assertEquals("age", item.getTable().getColumns().get(1).getName());
        directory.delete();
    }

    @Test
    public void fetchResultRecordSetReturnsNextCsvRow() throws IOException {
        File directory = createDirectory();
        writeCsv(directory, "people", "name;age\nAlice;30\n");

        FetchTableRowItem item = new FetchTableRowItem(directory.getAbsolutePath() + File.separator, "people");

        RecordSet result = item.fetchResultRecordSet();

        assertNotNull(result);
        assertEquals(2, result.getColumns().size());
        assertEquals(1, result.getRows().size());
        directory.delete();
    }

    @Test
    public void fetchResultRecordSetReturnsNullWhenNoRowsRemain() throws IOException {
        File directory = createDirectory();
        writeCsv(directory, "people", "name;age\n");

        FetchTableRowItem item = new FetchTableRowItem(directory.getAbsolutePath() + File.separator, "people");

        assertNull(item.fetchResultRecordSet());
        directory.delete();
    }

    @Test
    public void setAndGetTablePreservesTable() {
        FetchTableRowItem item = new FetchTableRowItem("", "people");
        Table table = new Table();

        item.setTable(table);

        assertSame(table, item.getTable());
    }

    @Test
    public void setAndGetTableNamePreservesName() {
        FetchTableRowItem item = new FetchTableRowItem("", "people");

        item.setTableName("customers");

        assertEquals("customers", item.getTableName());
    }

    private File createDirectory() throws IOException {
        File directory = File.createTempFile("fetch-table-row-item", "test");
        if (!directory.delete() || !directory.mkdir()) {
            throw new IOException("Could not create temporary directory");
        }
        return directory;
    }

    private void writeCsv(File directory, String tableName, String contents) throws IOException {
        File file = new File(directory, tableName + ".csv");
        FileWriter writer = new FileWriter(file);
        try {
            writer.write(contents);
        } finally {
            writer.close();
        }
    }
}
