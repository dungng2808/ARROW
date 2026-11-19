package lrn.plan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import lrn.recordset.RecordSet;
import org.junit.Test;

public class FetchTableRowItem_ArrowTest {

    @Test
    public void readsTableColumnsAndRows() throws IOException {
        File directory = Files.createTempDirectory("fetch-table-row-item").toFile();
        File csv = new File(directory, "people.csv");
        try {
            FileWriter writer = new FileWriter(csv);
            try {
                writer.write("name;age\nAlice;30\nBob;25\n");
            } finally {
                writer.close();
            }

            FetchTableRowItem item = new FetchTableRowItem(
                    directory.getAbsolutePath() + File.separator, "people");

            assertEquals("people", item.getTableName());
            assertEquals(2, item.getTable().getColumns().size());

            RecordSet first = item.fetchResultRecordSet();
            assertNotNull(first);
            assertEquals(1, first.getRows().size());

            RecordSet second = item.fetchResultRecordSet();
            assertNotNull(second);
            assertEquals(1, second.getRows().size());

            assertNull(item.fetchResultRecordSet());
        } finally {
            Files.deleteIfExists(csv.toPath());
            Files.deleteIfExists(directory.toPath());
        }
    }

    @Test
    public void settersUpdateTableAndName() {
        FetchTableRowItem item = new FetchTableRowItem("missing-directory/", "initial");
        item.setTableName("updated");

        assertEquals("updated", item.getTableName());
        assertNull(item.getTable());
    }

    @Test
    public void missingFileReturnsNullWhenFetching() {
        FetchTableRowItem item = new FetchTableRowItem("missing-directory/", "missing");
        assertNull(item.fetchResultRecordSet());
    }
}
