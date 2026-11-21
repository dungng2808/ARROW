package com.aliyun.odps.data;

import com.aliyun.odps.PartitionSpec;
import com.aliyun.odps.utils.StringUtils;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

public class TableInfo_ArrowTest {

    @Test
    public void testTableInfoConstructor() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20230101");
        String[] cols = {"col1", "col2"};
        String label = "test_label";

        TableInfo tableInfo = new TableInfo("project", "table", partSpec, cols, label);

        assertEquals("project", tableInfo.getProjectName());
        assertEquals("table", tableInfo.getTableName());
        assertEquals(partSpec, tableInfo.getPartSpec());
        assertArrayEquals(cols, tableInfo.getCols());
        assertEquals(label, tableInfo.getLabel());
    }

    @Test
    public void testTableInfoDefaultConstructor() {
        TableInfo tableInfo = new TableInfo();
        assertNull(tableInfo.getProjectName());
        assertNull(tableInfo.getTableName());
        assertNotNull(tableInfo.getPartSpec());
        assertNull(tableInfo.getCols());
        assertEquals(TableInfo.DEFAULT_LABEL, tableInfo.getLabel());
    }

    @Test
    public void testTableInfoCopyConstructor() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20230101");
        String[] cols = {"col1", "col2"};
        String label = "test_label";
        String mapperClassName = "com.example.Mapper";

        TableInfo original = new TableInfo("project", "table", partSpec, cols, label);
        original.setMapperClass(String.class);

        TableInfo copy = new TableInfo(original);

        assertEquals(original.getProjectName(), copy.getProjectName());
        assertEquals(original.getTableName(), copy.getTableName());
        assertEquals(original.getPartSpec(), copy.getPartSpec());
        assertArrayEquals(original.getCols(), copy.getCols());
        assertEquals(original.getLabel(), copy.getLabel());
        assertEquals(original.getMapperClass(), copy.getMapperClass());
    }

    @Test
    public void testBuilder() {
        TableInfo tableInfo = TableInfo.builder()
                .projectName("project")
                .tableName("table")
                .cols(new String[]{"col1", "col2"})
                .label("test_label")
                .build();

        assertEquals("project", tableInfo.getProjectName());
        assertEquals("table", tableInfo.getTableName());
        assertArrayEquals(new String[]{"col1", "col2"}, tableInfo.getCols());
        assertEquals("test_label", tableInfo.getLabel());
    }

    @Test
    public void testSetTableNameValid() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("test_table");

        assertEquals("test_table", tableInfo.getTableName());
    }

    @Test
    public void testSetTableNameWithProject() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("project.test_table");

        assertEquals("project", tableInfo.getProjectName());
        assertEquals("test_table", tableInfo.getTableName());
    }

    @Test(expected = RuntimeException.class)
    public void testSetTableNameEmpty() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("");
    }

    @Test(expected = RuntimeException.class)
    public void testSetTableNameNull() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName(null);
    }

    @Test(expected = RuntimeException.class)
    public void testSetTableNameInvalidFormat() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("invalid.format.table.name");
    }

    @Test
    public void testGetPartitionSpec() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20230101");
        partSpec.put("hr", "10");

        TableInfo tableInfo = new TableInfo();
        tableInfo.setPartSpec(partSpec);

        PartitionSpec partitionSpec = tableInfo.getPartitionSpec();
        assertEquals("20230101", partitionSpec.get("ds"));
        assertEquals("10", partitionSpec.get("hr"));
    }

    @Test
    public void testGetPartPath() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20230101");
        partSpec.put("hr", "10");

        TableInfo tableInfo = new TableInfo();
        tableInfo.setPartSpec(partSpec);

        String partPath = tableInfo.getPartPath();
        assertTrue(partPath.contains("ds=20230101/"));
        assertTrue(partPath.contains("hr=10/"));
    }

    @Test
    public void testSetPartSpecFromString() {
        String partPath = "ds=20230101/hr=10/";
        TableInfo tableInfo = new TableInfo();
        tableInfo.setPartSpec(partPath);

        LinkedHashMap<String, String> partSpec = tableInfo.getPartSpec();
        assertEquals("20230101", partSpec.get("ds"));
        assertEquals("10", partSpec.get("hr"));
    }

    @Test(expected = RuntimeException.class)
    public void testSetPartSpecFromStringInvalidFormat() {
        String partPath = "invalid_part_spec";
        TableInfo tableInfo = new TableInfo();
        tableInfo.setPartSpec(partPath);
    }

    @Test
    public void testEquals() {
        LinkedHashMap<String, String> partSpec1 = new LinkedHashMap<>();
        partSpec1.put("ds", "20230101");

        LinkedHashMap<String, String> partSpec2 = new LinkedHashMap<>();
        partSpec2.put("ds", "20230101");

        TableInfo tableInfo1 = new TableInfo("project", "table", partSpec1, new String[]{"col1"}, "label");
        TableInfo tableInfo2 = new TableInfo("project", "table", partSpec2, new String[]{"col1"}, "label");

        assertTrue(tableInfo1.equals(tableInfo2));
    }

    @Test
    public void testToString() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20230101");

        TableInfo tableInfo = new TableInfo("project", "table", partSpec, null, "label");
        String result = tableInfo.toString();

        assertTrue(result.contains("project.table"));
        assertTrue(result.contains("ds=20230101"));
    }

    @Test
    public void testValidateValid() {
        TableInfo tableInfo = new TableInfo("project", "table", new LinkedHashMap<>(), null, "label");
        // Should not throw exception
        tableInfo.validate();
    }

    @Test(expected = RuntimeException.class)
    public void testValidateInvalidTableName() {
        TableInfo tableInfo = new TableInfo("project", "", new LinkedHashMap<>(), null, "label");
        tableInfo.validate();
    }

    @Test
    public void testGetMapperClass() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setMapperClass(String.class);

        assertEquals(String.class, tableInfo.getMapperClass());
    }

    @Test
    public void testGetMapperClassNull() {
        TableInfo tableInfo = new TableInfo();
        assertNull(tableInfo.getMapperClass());
    }
}
