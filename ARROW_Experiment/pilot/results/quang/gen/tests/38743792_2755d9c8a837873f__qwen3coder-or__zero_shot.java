package com.aliyun.odps.data;

import com.aliyun.odps.PartitionSpec;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.LinkedHashMap;

public class TableInfo_ArrowTest {

    @Test
    public void testTableInfoConstructor() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");
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
        partSpec.put("ds", "20131230");
        String[] cols = {"col1", "col2"};
        String label = "test_label";
        String mapperClassName = "com.example.Mapper";

        TableInfo original = new TableInfo("project", "table", partSpec, cols, label);
        original.setMapperClass(TestMapper.class);

        TableInfo copy = new TableInfo(original);

        assertEquals(original.getProjectName(), copy.getProjectName());
        assertEquals(original.getTableName(), copy.getTableName());
        assertEquals(original.getPartSpec(), copy.getPartSpec());
        assertArrayEquals(original.getCols(), copy.getCols());
        assertEquals(original.getLabel(), copy.getLabel());
        assertEquals(original.getMapperClass(), copy.getMapperClass());
    }

    @Test
    public void testBuilderPattern() {
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
    public void testSetPartSpecWithLinkedHashMap() {
        TableInfo tableInfo = new TableInfo();
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");
        partSpec.put("hr", "10");

        tableInfo.setPartSpec(partSpec);
        assertEquals(partSpec, tableInfo.getPartSpec());
    }

    @Test
    public void testSetPartSpecWithPath() {
        TableInfo tableInfo = new TableInfo();
        String partPath = "ds=20131230/hr=10/";

        tableInfo.setPartSpec(partPath);
        LinkedHashMap<String, String> expected = new LinkedHashMap<>();
        expected.put("ds", "20131230");
        expected.put("hr", "10");
        assertEquals(expected, tableInfo.getPartSpec());
    }

    @Test
    public void testGetPartPath() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");
        partSpec.put("hr", "10");

        TableInfo tableInfo = new TableInfo();
        tableInfo.setPartSpec(partSpec);

        String partPath = tableInfo.getPartPath();
        assertEquals("ds=20131230/hr=10/", partPath);
    }

    @Test
    public void testGetPartitionSpec() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");
        partSpec.put("hr", "10");

        TableInfo tableInfo = new TableInfo();
        tableInfo.setPartSpec(partSpec);

        PartitionSpec partitionSpec = tableInfo.getPartitionSpec();
        assertEquals("20131230", partitionSpec.get("ds"));
        assertEquals("10", partitionSpec.get("hr"));
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
    public void testSetTableNameInvalid() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("");
    }

    @Test(expected = RuntimeException.class)
    public void testValidateEmptyTableName() {
        TableInfo tableInfo = new TableInfo(null, "", null, null, null);
        tableInfo.validate();
    }

    @Test
    public void testToString() {
        LinkedHashMap<String, String> partSpec = new LinkedHashMap<>();
        partSpec.put("ds", "20131230");

        TableInfo tableInfo = new TableInfo("project", "table", partSpec, null, null);
        String result = tableInfo.toString();
        assertEquals("project.table{ds=20131230}", result);
    }

    @Test
    public void testEquals() {
        LinkedHashMap<String, String> partSpec1 = new LinkedHashMap<>();
        partSpec1.put("ds", "20131230");
        String[] cols1 = {"col1", "col2"};

        LinkedHashMap<String, String> partSpec2 = new LinkedHashMap<>();
        partSpec2.put("ds", "20131230");
        String[] cols2 = {"col1", "col2"};

        TableInfo tableInfo1 = new TableInfo("project", "table", partSpec1, cols1, "label");
        TableInfo tableInfo2 = new TableInfo("project", "table", partSpec2, cols2, "label");

        assertTrue(tableInfo1.equals(tableInfo2));
    }

    @Test
    public void testEqualsDifferent() {
        LinkedHashMap<String, String> partSpec1 = new LinkedHashMap<>();
        partSpec1.put("ds", "20131230");
        String[] cols1 = {"col1", "col2"};

        LinkedHashMap<String, String> partSpec2 = new LinkedHashMap<>();
        partSpec2.put("ds", "20131230");
        String[] cols2 = {"col1", "col2"};

        TableInfo tableInfo1 = new TableInfo("project", "table", partSpec1, cols1, "label1");
        TableInfo tableInfo2 = new TableInfo("project", "table", partSpec2, cols2, "label2");

        assertFalse(tableInfo1.equals(tableInfo2));
    }

    @Test
    public void testSetMapperClass() {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setMapperClass(TestMapper.class);

        assertEquals(TestMapper.class, tableInfo.getMapperClass());
    }

    @Test
    public void testGetMapperClassNull() {
        TableInfo tableInfo = new TableInfo();
        assertNull(tableInfo.getMapperClass());
    }

    public static class TestMapper {
    }
}
