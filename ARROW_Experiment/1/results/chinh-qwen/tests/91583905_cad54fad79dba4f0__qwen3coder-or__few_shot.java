package org.alfasoftware.morf.upgrade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.metadata.Index;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.metadata.Table;
import org.junit.Test;

public class ChangeIndex_ArrowTest {

  @Test
  public void testGetTableName() {
    Index fromIndex = mock(Index.class);
    Index toIndex = mock(Index.class);
    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    assertEquals("testTable", changeIndex.getTableName());
  }

  @Test
  public void testGetFromIndex() {
    Index fromIndex = mock(Index.class);
    Index toIndex = mock(Index.class);
    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    assertEquals(fromIndex, changeIndex.getFromIndex());
  }

  @Test
  public void testGetToIndex() {
    Index fromIndex = mock(Index.class);
    Index toIndex = mock(Index.class);
    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    assertEquals(toIndex, changeIndex.getToIndex());
  }

  @Test
  public void testAccept() {
    Index fromIndex = mock(Index.class);
    Index toIndex = mock(Index.class);
    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    SchemaChangeVisitor visitor = mock(SchemaChangeVisitor.class);
    changeIndex.accept(visitor);
  }

  @Test
  public void testApply() {
    Index fromIndex = mock(Index.class);
    Index toIndex = mock(Index.class);
    when(fromIndex.getName()).thenReturn("oldIndex");
    when(toIndex.getName()).thenReturn("newIndex");

    Table table = mock(Table.class);
    when(table.indexes()).thenReturn(Arrays.asList(fromIndex));

    Schema schema = mock(Schema.class);
    when(schema.getTable("testTable")).thenReturn(table);

    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    Schema result = changeIndex.apply(schema);
    // Just checking that it doesn't throw an exception
    assertTrue(result != null);
  }

  @Test
  public void testReverse() {
    Index fromIndex = mock(Index.class);
    Index toIndex = mock(Index.class);
    when(fromIndex.getName()).thenReturn("oldIndex");
    when(toIndex.getName()).thenReturn("newIndex");

    Table table = mock(Table.class);
    when(table.indexes()).thenReturn(Arrays.asList(toIndex));

    Schema schema = mock(Schema.class);
    when(schema.getTable("testTable")).thenReturn(table);

    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    Schema result = changeIndex.reverse(schema);
    // Just checking that it doesn't throw an exception
    assertTrue(result != null);
  }

  @Test
  public void testIsAppliedWhenTableDoesNotExist() {
    Schema schema = mock(Schema.class);
    when(schema.tableExists("nonExistentTable")).thenReturn(false);
    
    Index fromIndex = mock(Index.class);
    Index toIndex = mock(Index.class);
    ChangeIndex changeIndex = new ChangeIndex("nonExistentTable", fromIndex, toIndex);
    
    ConnectionResources database = mock(ConnectionResources.class);
    assertFalse(changeIndex.isApplied(schema, database));
  }

  @Test
  public void testIsAppliedWhenIndexExists() {
    Index fromIndex = mock(Index.class);
    Index toIndex = mock(Index.class);
    when(fromIndex.getName()).thenReturn("oldIndex");
    when(toIndex.getName()).thenReturn("newIndex");

    Table table = mock(Table.class);
    when(table.indexes()).thenReturn(Arrays.asList(toIndex));

    Schema schema = mock(Schema.class);
    when(schema.tableExists("testTable")).thenReturn(true);
    when(schema.getTable("testTable")).thenReturn(table);

    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    ConnectionResources database = mock(ConnectionResources.class);
    assertTrue(changeIndex.isApplied(schema, database));
  }

  @Test
  public void testIsAppliedWhenIndexDoesNotExist() {
    Index fromIndex = mock(Index.class);
    Index toIndex = mock(Index.class);
    when(fromIndex.getName()).thenReturn("oldIndex");
    when(toIndex.getName()).thenReturn("newIndex");

    Table table = mock(Table.class);
    when(table.indexes()).thenReturn(Arrays.asList(fromIndex));

    Schema schema = mock(Schema.class);
    when(schema.tableExists("testTable")).thenReturn(true);
    when(schema.getTable("testTable")).thenReturn(table);

    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    ConnectionResources database = mock(ConnectionResources.class);
    assertFalse(changeIndex.isApplied(schema, database));
  }

  @Test(expected = IllegalStateException.class)
  public void testApplyChangeWithNullStartPoint() {
    Index fromIndex = null;
    Index toIndex = mock(Index.class);
    when(toIndex.getName()).thenReturn("newIndex");

    Table table = mock(Table.class);
    when(table.indexes()).thenReturn(Collections.emptyList());

    Schema schema = mock(Schema.class);
    when(schema.getTable("testTable")).thenReturn(table);

    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    changeIndex.apply(schema);
  }

  @Test(expected = IllegalStateException.class)
  public void testApplyChangeWithNullEndPoint() {
    Index fromIndex = mock(Index.class);
    when(fromIndex.getName()).thenReturn("oldIndex");
    Index toIndex = null;

    Table table = mock(Table.class);
    when(table.indexes()).thenReturn(Collections.emptyList());

    Schema schema = mock(Schema.class);
    when(schema.getTable("testTable")).thenReturn(table);

    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    changeIndex.apply(schema);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testApplyChangeWithNonExistentIndex() {
    Index fromIndex = mock(Index.class);
    when(fromIndex.getName()).thenReturn("oldIndex");
    Index toIndex = mock(Index.class);
    when(toIndex.getName()).thenReturn("newIndex");

    Table table = mock(Table.class);
    when(table.indexes()).thenReturn(Collections.emptyList());

    Schema schema = mock(Schema.class);
    when(schema.getTable("testTable")).thenReturn(table);

    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    changeIndex.apply(schema);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testApplyChangeWithDuplicateIndexName() {
    Index fromIndex = mock(Index.class);
    when(fromIndex.getName()).thenReturn("oldIndex");
    Index toIndex = mock(Index.class);
    when(toIndex.getName()).thenReturn("oldIndex");

    Table table = mock(Table.class);
    when(table.indexes()).thenReturn(Arrays.asList(fromIndex, toIndex));

    Schema schema = mock(Schema.class);
    when(schema.getTable("testTable")).thenReturn(table);

    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    changeIndex.apply(schema);
  }

  @Test
  public void testToString() {
    Index fromIndex = mock(Index.class);
    when(fromIndex.getName()).thenReturn("fromIndex");
    Index toIndex = mock(Index.class);
    when(toIndex.getName()).thenReturn("toIndex");
    
    ChangeIndex changeIndex = new ChangeIndex("testTable", fromIndex, toIndex);
    String result = changeIndex.toString();
    assertTrue(result.contains("ChangeIndex"));
    assertTrue(result.contains("testTable"));
    assertTrue(result.contains("fromIndex"));
    assertTrue(result.contains("toIndex"));
  }
}
