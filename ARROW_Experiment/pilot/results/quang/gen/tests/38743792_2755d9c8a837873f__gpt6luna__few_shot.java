package com.aliyun.odps.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    String[] columns = {"id", "name"};

    TableInfo table = new TableInfo("project", "users", parts, columns, "input");

    assertEquals("project", table.getProjectName());
    assertEquals("users", table.getTableName());
    assertEquals(parts, table.getPartSpec());
    assertArrayEquals(columns, table.getCols());
    assertEquals("input", table.getLabel());
  }

  @Test
  public void defaultConstructorUsesDefaultLabelAndEmptyPartitionSpec() {
    TableInfo table = new TableInfo();

    assertEquals(TableInfo.DEFAULT_LABEL, table.getLabel());
    assertEquals("", table.getPartPath());
    assertEquals("null", table.toString());
    assertNull(table.getMapperClass());
  }

  @Test
  public void setTableNameParsesProjectAndTable() {
    TableInfo table = new TableInfo();
    table.setTableName(" project.users ");

    assertEquals(" project", table.getProjectName());
    assertEquals("users ", table.getTableName());
    assertEquals(" project.users ", table.toString());
  }

  @Test
  public void setTableNameWithoutProjectPreservesExistingProject() {
    TableInfo table = new TableInfo();
    table.setProjectName("project");
    table.setTableName("users");

    assertEquals("project", table.getProjectName());
    assertEquals("users", table.getTableName());
  }

  @Test
  public void setPartSpecPathParsesOrderedPartitions() {
    TableInfo table = new TableInfo();
    table.setPartSpec("ds=20240101/hr=10");

    assertEquals("ds=20240101/hr=10/", table.getPartPath());
    assertEquals("{ds=20240101, hr=10}", table.getPartSpec().toString());
    assertEquals("ds=20240101", table.getPartitionSpec().get("ds"));
    assertEquals("hr=10", table.getPartitionSpec().get("hr"));
  }

  @Test
  public void nullPartitionPathProducesEmptyPartitions() {
    TableInfo table = new TableInfo();
    table.setPartSpec((String) null);

    assertEquals("", table.getPartPath());
    assertTrue(table.getPartSpec().isEmpty());
  }

  @Test
  public void toStringIncludesProjectAndPartitions() {
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>();
    parts.put("ds", "20240101");
    TableInfo table = new TableInfo("project", "users", parts, null, TableInfo.DEFAULT_LABEL);

    assertEquals("project.users{ds=20240101}", table.toString());
  }

  @Test
  public void builderCreatesTableInfoWithConfiguredValues() {
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>();
    parts.put("ds", "20240101");
    String[] columns = {"id"};

    TableInfo table = TableInfo.builder()
        .projectName("project")
        .tableName("users")
        .partSpec(parts)
        .cols(columns)
        .label("output")
        .build();

    assertEquals("project", table.getProjectName());
    assertEquals("users", table.getTableName());
    assertEquals(parts, table.getPartSpec());
    assertArrayEquals(columns, table.getCols());
    assertEquals("output", table.getLabel());
  }

  @Test
  public void builderCanSetMapperClass() {
    TableInfo table = TableInfo.builder()
        .tableName("users")
        .mapperClass(String.class)
        .build();

    assertEquals(String.class, table.getMapperClass());
  }

  @Test
  public void copyConstructorCopiesValuesAndMapperClass() {
    LinkedHashMap<String, String> parts = new LinkedHashMap<String, String>();
    parts.put("ds", "20240101");
    TableInfo original = TableInfo.builder()
        .projectName("project")
        .tableName("users")
        .partSpec(parts)
        .cols(new String[] {"id"})
        .label("input")
        .mapperClass(String.class)
        .build();

    TableInfo copy = new TableInfo(original);

    assertEquals(original, copy);
    assertEquals(String.class, copy.getMapperClass());
  }

  @Test
  public void equalsComparesTableValues() {
    LinkedHashMap<String, String> firstParts = new LinkedHashMap<String, String>();
    firstParts.put("ds", "20240101");
    LinkedHashMap<String, String> secondParts = new LinkedHashMap<String, String>();
    secondParts.put("ds", "20240101");

    TableInfo first = new TableInfo("project", "users", firstParts, new String[] {"id"}, "input");
    TableInfo second = new TableInfo("project", "users", secondParts, new String[] {"id"}, "input");

    assertTrue(first.equals(second));
    assertFalse(first.equals("users"));
  }

  @Test
  public void validateAcceptsNonEmptyTableName() {
    TableInfo table = new TableInfo();
    table.setTableName("users");

    table.validate();
    assertEquals("users", table.getTableName());
  }

  @Test
  public void setTableNameRejectsNull() {
    TableInfo table = new TableInfo();

    try {
      table.setTableName(null);
      fail("Expected invalid table name to be rejected");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("table should not be null or empty"));
    }
  }

  @Test
  public void setTableNameRejectsMalformedQualifiedName() {
    TableInfo table = new TableInfo();

    try {
      table.setTableName("project.");
      fail("Expected malformed table name to be rejected");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("error table format"));
    }
  }

  @Test
  public void setPartSpecRejectsMalformedPath() {
    TableInfo table = new TableInfo();

    try {
      table.setPartSpec("ds");
      fail("Expected malformed partition path to be rejected");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("error part spec format"));
    }
  }
}
