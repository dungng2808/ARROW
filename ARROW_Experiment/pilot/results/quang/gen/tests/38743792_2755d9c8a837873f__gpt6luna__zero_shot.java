package com.aliyun.odps.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedHashMap;

import org.junit.Test;

public class TableInfo_ArrowTest {

  @Test
  public void constructorAndGettersReturnProvidedValues() {
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>();
    parts.put("ds", "20240101");
    String[] cols = new String[] {"id", "name"};

    TableInfo table = new TableInfo("project", "orders", parts, cols, "input");

    assertEquals("project", table.getProjectName());
    assertEquals("orders", table.getTableName());
    assertEquals(parts, table.getPartSpec());
    assertArrayEquals(cols, table.getCols());
    assertEquals("input", table.getLabel());
  }

  @Test
  public void defaultConstructorHasDefaultLabelAndEmptyPartition() {
    TableInfo table = new TableInfo();

    assertEquals(TableInfo.DEFAULT_LABEL, table.getLabel());
    assertEquals("", table.getPartPath());
    assertNull(table.getProjectName());
    assertNull(table.getTableName());
  }

  @Test
  public void builderSetsTableProperties() {
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>();
    parts.put("region", "west");
    String[] cols = new String[] {"code"};

    TableInfo table = TableInfo.builder()
        .projectName("analytics")
        .tableName("events")
        .partSpec(parts)
        .cols(cols)
        .label("output")
        .mapperClass(String.class)
        .build();

    assertEquals("analytics", table.getProjectName());
    assertEquals("events", table.getTableName());
    assertEquals(parts, table.getPartSpec());
    assertArrayEquals(cols, table.getCols());
    assertEquals("output", table.getLabel());
    assertEquals(String.class, table.getMapperClass());
  }

  @Test
  public void tableNameWithProjectIsSplitIntoProjectAndTable() {
    TableInfo table = new TableInfo();
    table.setTableName("  sales . orders  ");

    assertEquals("sales ", table.getProjectName());
    assertEquals(" orders", table.getTableName());
  }

  @Test
  public void tableNameWithoutProjectPreservesExistingProject() {
    TableInfo table = new TableInfo();
    table.setProjectName("existing");
    table.setTableName("orders");

    assertEquals("existing", table.getProjectName());
    assertEquals("orders", table.getTableName());
  }

  @Test
  public void partPathIsParsedAndRenderedInInsertionOrder() {
    TableInfo table = new TableInfo();
    table.setPartSpec("ds=20240101/hr=10");

    assertEquals("ds=20240101/hr=10/", table.getPartPath());
    assertEquals("{ds=20240101, hr=10}", table.getPartSpec().toString());
    assertEquals("ds=20240101/hr=10/", table.getPartPath());
  }

  @Test
  public void nullOrBlankPartPathProducesEmptyPartition() {
    TableInfo table = new TableInfo();
    table.setPartSpec((String) null);
    assertEquals("", table.getPartPath());

    table.setPartSpec("   ");
    assertEquals("", table.getPartPath());
  }

  @Test
  public void partitionSpecObjectIsCreatedFromPartitionMap() {
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>();
    parts.put("ds", "20240101");
    TableInfo table = new TableInfo("p", "t", parts, null, TableInfo.DEFAULT_LABEL);

    assertNotNull(table.getPartitionSpec());
    assertEquals("20240101", table.getPartitionSpec().get("ds"));
  }

  @Test
  public void toStringIncludesProjectTableAndPartitions() {
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>();
    parts.put("ds", "20240101");
    TableInfo table = new TableInfo("project", "orders", parts, null, TableInfo.DEFAULT_LABEL);

    assertEquals("project.orders{ds=20240101}", table.toString());
  }

  @Test
  public void equalsComparesTableProperties() {
    LinkedHashMap<String, String> firstParts = new LinkedHashMap<String, String>();
    firstParts.put("ds", "20240101");
    LinkedHashMap<String, String> secondParts = new LinkedHashMap<String, String>();
    secondParts.put("ds", "20240101");

    TableInfo first =
        new TableInfo("project", "orders", firstParts, new String[] {"id"}, "input");
    TableInfo second =
        new TableInfo("project", "orders", secondParts, new String[] {"id"}, "input");

    assertTrue(first.equals(second));
    assertFalse(first.equals("orders"));
  }

  @Test
  public void copyConstructorCopiesTableValues() {
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>();
    parts.put("ds", "20240101");
    TableInfo original =
        new TableInfo("project", "orders", parts, new String[] {"id"}, "input");
    original.setMapperClass(Integer.class);

    TableInfo copy = new TableInfo(original);

    assertEquals("project", copy.getProjectName());
    assertEquals("orders", copy.getTableName());
    assertEquals(parts, copy.getPartSpec());
    assertArrayEquals(new String[] {"id"}, copy.getCols());
    assertEquals("input", copy.getLabel());
    assertEquals(Integer.class, copy.getMapperClass());
  }

  @Test
  public void mapperClassIsNullUntilSet() {
    TableInfo table = new TableInfo();

    assertNull(table.getMapperClass());
  }

  @Test
  public void settersUpdateColumnsAndLabel() {
    TableInfo table = new TableInfo();
    String[] cols = new String[] {"a", "b"};

    table.setCols(cols);
    table.setLable("custom");

    assertArrayEquals(cols, table.getCols());
    assertEquals("custom", table.getLabel());
  }

  @Test
  public void validateRejectsMissingTableName() {
    TableInfo table = new TableInfo();

    try {
      table.validate();
      fail("Expected invalid table name to be rejected");
    } catch (RuntimeException expected) {
      assertEquals("ODPS-0730001: table should not be null or empty: null", expected.getMessage());
    }
  }

  @Test
  public void setTableNameRejectsNullName() {
    TableInfo table = new TableInfo();

    try {
      table.setTableName(null);
      fail("Expected null table name to be rejected");
    } catch (RuntimeException expected) {
      assertEquals("ODPS-0730001: table should not be null or empty: null", expected.getMessage());
    }
  }

  @Test
  public void setTableNameRejectsMalformedQualifiedName() {
    TableInfo table = new TableInfo();

    try {
      table.setTableName("project.table.extra");
      fail("Expected malformed table name to be rejected");
    } catch (RuntimeException expected) {
      assertEquals("ODPS-0730001: error table format: project.table.extra", expected.getMessage());
    }
  }

  @Test
  public void setPartSpecRejectsMalformedPath() {
    TableInfo table = new TableInfo();

    try {
      table.setPartSpec("ds=20240101/hr");
      fail("Expected malformed partition path to be rejected");
    } catch (RuntimeException expected) {
      assertEquals("ODPS-0730001: error part spec format: ds=20240101/hr", expected.getMessage());
    }
  }
}
