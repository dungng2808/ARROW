package com.aliyun.odps.data;

import com.aliyun.odps.PartitionSpec;
import java.util.LinkedHashMap;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TableInfo_ArrowTest {

  @Test
  public void builderCreatesTableWithConfiguredValues() {
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>();
    parts.put("ds", "20200101");
    String[] cols = new String[] {"id", "name"};

    TableInfo table = TableInfo.builder()
        .projectName("project")
        .tableName("events")
        .partSpec(parts)
        .cols(cols)
        .label("input")
        .build();

    assertEquals("project", table.getProjectName());
    assertEquals("events", table.getTableName());
    assertEquals("input", table.getLabel());
    assertEquals("ds=20200101/", table.getPartPath());
    assertArrayEquals(cols, table.getCols());
  }

  @Test
  public void defaultBuilderValuesArePreserved() {
    TableInfo table = TableInfo.builder().tableName("events").build();

    assertEquals(TableInfo.DEFAULT_LABEL, table.getLabel());
    assertNull(table.getProjectName());
    assertEquals("", table.getPartPath());
  }

  @Test
  public void tableNameParsesProjectAndTable() {
    TableInfo table = new TableInfo();
    table.setTableName("project.events");

    assertEquals("project", table.getProjectName());
    assertEquals("events", table.getTableName());
  }

  @Test
  public void unqualifiedTableNameSetsOnlyTable() {
    TableInfo table = new TableInfo();
    table.setTableName("events");

    assertNull(table.getProjectName());
    assertEquals("events", table.getTableName());
  }

  @Test
  public void setPartSpecFromPathParsesOrderedValues() {
    TableInfo table = new TableInfo();
    table.setPartSpec("ds=20200101/hr=10");

    assertEquals("ds=20200101/hr=10/", table.getPartPath());
    assertEquals("20200101", table.getPartSpec().get("ds"));
    assertEquals("10", table.getPartSpec().get("hr"));
  }

  @Test
  public void nullPartPathCreatesEmptySpec() {
    TableInfo table = new TableInfo();
    table.setPartSpec((String) null);

    assertTrue(table.getPartSpec().isEmpty());
    assertEquals("", table.getPartPath());
  }

  @Test
  public void partitionSpecConstructorIsAvailableAndEmptyPartitionIsEmpty() {
    TableInfo table = TableInfo.builder().tableName("events").partSpec(new PartitionSpec()).build();

    assertTrue(table.getPartSpec().isEmpty());
  }

  @Test
  public void getPartitionSpecReturnsPartitionValues() {
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>();
    parts.put("ds", "20200101");
    TableInfo table = new TableInfo("project", "events", parts, null, TableInfo.DEFAULT_LABEL);

    PartitionSpec partitionSpec = table.getPartitionSpec();

    assertEquals("20200101", partitionSpec.get("ds"));
  }

  @Test
  public void constructorStoresColumnsAndLabel() {
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>();
    String[] cols = new String[] {"id", "value"};
    TableInfo table = new TableInfo("project", "events", parts, cols, "output");

    assertArrayEquals(cols, table.getCols());
    assertEquals("output", table.getLabel());
  }

  @Test
  public void setColsReplacesColumns() {
    TableInfo table = new TableInfo();
    String[] cols = new String[] {"id"};
    table.setCols(cols);

    assertSame(cols, table.getCols());
  }

  @Test
  public void toStringIncludesProjectAndPartitions() {
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>();
    parts.put("ds", "20200101");
    TableInfo table = new TableInfo("project", "events", parts, null, TableInfo.DEFAULT_LABEL);

    assertEquals("project.events{ds=20200101}", table.toString());
  }

  @Test
  public void toStringWithoutProjectUsesTableName() {
    TableInfo table = new TableInfo();
    table.setTableName("events");

    assertEquals("events", table.toString());
  }

  @Test
  public void equalsComparesTableDetails() {
    LinkedHashMap<String, String> parts1 = new LinkedHashMap<String, String>();
    parts1.put("ds", "20200101");
    LinkedHashMap<String, String> parts2 = new LinkedHashMap<String, String>();
    parts2.put("ds", "20200101");

    TableInfo first = new TableInfo("project", "events", parts1, new String[] {"id"},
                                    TableInfo.DEFAULT_LABEL);
    TableInfo second = new TableInfo("project", "events", parts2, new String[] {"id"},
                                     TableInfo.DEFAULT_LABEL);

    assertTrue(first.equals(second));
    assertFalse(first.equals("events"));
  }

  @Test
  public void mapperClassCanBeConfiguredAndRetrieved() {
    TableInfo table = TableInfo.builder().tableName("events").mapperClass(String.class).build();

    assertEquals(String.class, table.getMapperClass());
  }

  @Test
  public void mapperClassIsNullByDefault() {
    TableInfo table = new TableInfo();

    assertNull(table.getMapperClass());
  }

  @Test
  public void validateAcceptsNonEmptyTableName() {
    TableInfo table = new TableInfo();
    table.setTableName("events");

    table.validate();
  }

  @Test
  public void invalidTableNameThrowsRuntimeException() {
    TableInfo table = new TableInfo();
    try {
      table.setTableName(" ");
      fail("Expected invalid table name to throw");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("ODPS-0730001"));
    }
  }

  @Test
  public void invalidPartPathThrowsRuntimeException() {
    TableInfo table = new TableInfo();
    try {
      table.setPartSpec("ds=20200101/hr");
      fail("Expected invalid partition path to throw");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("ODPS-0730001"));
    }
  }
}
