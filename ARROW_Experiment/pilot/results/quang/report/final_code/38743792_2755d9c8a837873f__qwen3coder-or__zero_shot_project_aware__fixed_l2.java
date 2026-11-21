package com.aliyun.odps.data;

import static org.junit.Assert.*;
import java.util.LinkedHashMap;
import org.junit.Test;

public class TableInfo_ArrowTest {

    @Test
    public void testTableInfoConstructor() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");
        String[] cols = {"col1", "col2"};
        TableInfo tableInfo = new TableInfo("project", "table", partSpec, cols, "label");
        
        assertEquals("project", tableInfo.getProjectName());
        assertEquals("table", tableInfo.getTableName());
        assertEquals(partSpec, tableInfo.getPartSpec());
        assertArrayEquals(cols, tableInfo.getCols());
        assertEquals("label", tableInfo.getLabel());
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
        partSpec.put("ds", "20131230");
        String[] cols = {"col1", "col2"};
        TableInfo original = new TableInfo("project", "table", partSpec, cols, "label");
        original.setMapperClass(String.class);
        
        TableInfo copy = new TableInfo(original);
        
        assertEquals("project", copy.getProjectName());
        assertEquals("table", copy.getTableName());
        assertEquals(partSpec, copy.getPartSpec());
        assertArrayEquals(cols, copy.getCols());
        assertEquals("label", copy.getLabel());
        assertEquals(String.class, copy.getMapperClass());
    }

    @Test
    public void testBuilder() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");
        String[] cols = {"col1", "col2"};
        
        TableInfo tableInfo = TableInfo.builder()
                .projectName("project")
                .tableName("table")
                .partSpec(partSpec)
                .cols(cols)
                .label("label")
                .build();
        
        assertEquals("project", tableInfo.getProjectName());
        assertEquals("table", tableInfo.getTableName());
        assertEquals(partSpec, tableInfo.getPartSpec());
        assertArrayEquals(cols, tableInfo.getCols());
        assertEquals("label", tableInfo.getLabel());
    }

    @Test
    public void testGetPartitionSpec() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");
        partSpec.put("hr", "10");
        TableInfo tableInfo = new TableInfo("project", "table", partSpec, null, "label");
        
        com.aliyun.odps.PartitionSpec partitionSpec = tableInfo.getPartitionSpec();
        assertEquals("20131230", partitionSpec.get("ds"));
        assertEquals("10", partitionSpec.get("hr"));
    }

    @Test
    public void testGetPartPath() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");
        partSpec.put("hr", "10");
        TableInfo tableInfo = new TableInfo("project", "table", partSpec, null, "label");
        
        String partPath = tableInfo.getPartPath();
        assertEquals("ds=20131230/hr=10/", partPath);
    }

    @Test
    public void testSetPartSpecLinkedHashMap() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");
        TableInfo tableInfo = new TableInfo();
        tableInfo.setPartSpec(partSpec);
        
        assertEquals(partSpec, tableInfo.getPartSpec());
    }

    @Test
    public void testSetPartSpecString() {
        String partPath = "ds=20131230/hr=10/";
        TableInfo tableInfo = new TableInfo();
        tableInfo.setPartSpec(partPath);
        
        LinkedHashMap<String, String> expected = new LinkedHashMap<>();
        expected.put("ds", "20131230");
        expected.put("hr", "10");
        assertEquals(expected, tableInfo.getPartSpec());
    }

    @Test
    public void testGetCols() {
        String[] cols = {"col1", "col2"};
        TableInfo tableInfo = new TableInfo();
        tableInfo.setCols(cols);
        
        assertArrayEquals(cols, tableInfo.getCols());
    }

    @Test
    public void testSetCols() {
        String[] cols = {"col1", "col2"};
        TableInfo tableInfo = new TableInfo();
        tableInfo.setCols(cols);
        
        assertArrayEquals(cols, tableInfo.getCols());
    }

    @Test
    public void testGetTableName() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("table");
        
        assertEquals("table", tableInfo.getTableName());
    }

    @Test
    public void testSetTableNameWithProject() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("project.table");
        
        assertEquals("project", tableInfo.getProjectName());
        assertEquals("table", tableInfo.getTableName());
    }

    @Test
    public void testSetTableNameWithoutProject() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("table");
        
        assertNull(tableInfo.getProjectName());
        assertEquals("table", tableInfo.getTableName());
    }

    @Test(expected = RuntimeException.class)
    public void testSetTableNameEmpty() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("");
    }

    @Test(expected = RuntimeException.class)
    public void testSetTableNameInvalidFormat() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("project.table.extra");
    }

    @Test
    public void testGetLabel() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setLable("label");
        
        assertEquals("label", tableInfo.getLabel());
    }

    @Test
    public void testSetLable() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setLable("label");
        
        assertEquals("label", tableInfo.getLabel());
    }

    @Test
    public void testGetMapperClass() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setMapperClass(String.class);
        
        assertEquals(String.class, tableInfo.getMapperClass());
    }

    @Test
    public void testSetMapperClass() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setMapperClass(String.class);
        
        assertEquals("java.lang.String", tableInfo.getMapperClass().getName());
    }

    @Test
    public void testSetProjectName() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setProjectName("project");
        
        assertEquals("project", tableInfo.getProjectName());
    }

    @Test
    public void testEquals() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");
        String[] cols = {"col1", "col2"};
        
        TableInfo tableInfo1 = new TableInfo("project", "table", partSpec, cols, "label");
        TableInfo tableInfo2 = new TableInfo("project", "table", partSpec, cols, "label");
        
        assertTrue(tableInfo1.equals(tableInfo2));
    }

    @Test
    public void testToString() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");
        TableInfo tableInfo = new TableInfo("project", "table", partSpec, null, "label");
        
        assertEquals("project.table{ds=20131230}", tableInfo.toString());
    }

    @Test
    public void testValidateValid() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");
        TableInfo tableInfo = new TableInfo("project", "table", partSpec, null, "label");
        
        // Should not throw exception
        tableInfo.validate();
    }

    @Test(expected = RuntimeException.class)
    public void testValidateInvalidTableName() {
        TableInfo tableInfo = new TableInfo("project", "", null, null, "label");
        tableInfo.validate();
    }
}
