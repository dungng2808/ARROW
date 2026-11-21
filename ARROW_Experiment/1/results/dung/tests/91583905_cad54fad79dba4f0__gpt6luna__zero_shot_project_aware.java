package org.alfasoftware.morf.upgrade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.metadata.Index;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.metadata.Table;
import org.junit.Test;

public class ChangeIndex_ArrowTest {

  @Test
  public void gettersReturnConstructorValues() {
    Index from = mock(Index.class);
    Index to = mock(Index.class);
    ChangeIndex change = new ChangeIndex("sample", from, to);

    assertEquals("sample", change.getTableName());
    assertSame(from, change.getFromIndex());
    assertSame(to, change.getToIndex());
  }

  @Test
  public void toStringIncludesValues() {
    Index from = mock(Index.class);
    Index to = mock(Index.class);
    ChangeIndex change = new ChangeIndex("sample", from, to);

    assertEquals("ChangeIndex [tableName=sample, fromIndex=" + from + ", toIndex=" + to + "]", change.toString());
  }

  @Test
  public void acceptVisitsThisChange() {
    Index from = mock(Index.class);
    Index to = mock(Index.class);
    SchemaChangeVisitor visitor = mock(SchemaChangeVisitor.class);
    ChangeIndex change = new ChangeIndex("sample", from, to);

    change.accept(visitor);

    verify(visitor).visit(change);
  }

  @Test
  public void isAppliedReturnsFalseWhenTableDoesNotExist() {
    Schema schema = mock(Schema.class);
    when(schema.tableExists("sample")).thenReturn(false);
    ChangeIndex change = new ChangeIndex("sample", mock(Index.class), mock(Index.class));

    assertFalse(change.isApplied(schema, mock(ConnectionResources.class)));
  }

  @Test
  public void isAppliedReturnsFalseWhenTableHasNoIndexes() {
    Schema schema = mock(Schema.class);
    Table table = mock(Table.class);
    when(schema.tableExists("sample")).thenReturn(true);
    when(schema.getTable("sample")).thenReturn(table);
    when(table.indexes()).thenReturn(Collections.<Index>emptyList());
    ChangeIndex change = new ChangeIndex("sample", mock(Index.class), mock(Index.class));

    assertFalse(change.isApplied(schema, mock(ConnectionResources.class)));
  }

  @Test
  public void isAppliedReturnsTrueWhenTargetIndexMatches() {
    Index target = mock(Index.class);
    Index existing = mock(Index.class);
    Schema schema = mock(Schema.class);
    Table table = mock(Table.class);
    when(schema.tableExists("sample")).thenReturn(true);
    when(schema.getTable("sample")).thenReturn(table);
    when(table.indexes()).thenReturn(Arrays.asList(existing));
    when(existing.getName()).thenReturn("IDX");
    when(target.getName()).thenReturn("idx");
    when(existing.columns()).thenReturn(Collections.<String>emptyList());
    when(target.columns()).thenReturn(Collections.<String>emptyList());
    ChangeIndex change = new ChangeIndex("sample", mock(Index.class), target);

    assertTrue(change.isApplied(schema, mock(ConnectionResources.class)));
  }

  @Test
  public void applyReturnsSchemaWithChangedIndex() {
    Index from = mock(Index.class);
    Index to = mock(Index.class);
    Table table = mock(Table.class);
    Schema schema = mock(Schema.class);
    when(schema.getTable("sample")).thenReturn(table);
    when(table.indexes()).thenReturn(Arrays.asList(from));
    when(from.getName()).thenReturn("old");
    when(to.getName()).thenReturn("new");
    ChangeIndex change = new ChangeIndex("sample", from, to);

    Schema result = change.apply(schema);

    assertNotNull(result);
    assertTrue(result.tableExists("sample"));
  }

  @Test
  public void applyThrowsWhenStartIndexIsNull() {
    ChangeIndex change = new ChangeIndex("sample", null, mock(Index.class));

    try {
      change.apply(mock(Schema.class));
    } catch (IllegalStateException expected) {
      assertEquals("Cannot change a null index to have a new definition", expected.getMessage());
      return;
    }
    throw new AssertionError("Expected IllegalStateException");
  }

  @Test
  public void applyThrowsWhenEndIndexIsNull() {
    Index from = mock(Index.class);
    when(from.getName()).thenReturn("old");
    ChangeIndex change = new ChangeIndex("sample", from, null);

    try {
      change.apply(mock(Schema.class));
    } catch (IllegalStateException expected) {
      assertEquals("Cannot change index [old] to be null", expected.getMessage());
      return;
    }
    throw new AssertionError("Expected IllegalStateException");
  }

  @Test
  public void applyThrowsWhenStartIndexIsMissing() {
    Index from = mock(Index.class);
    Index to = mock(Index.class);
    Table table = mock(Table.class);
    Schema schema = mock(Schema.class);
    when(schema.getTable("sample")).thenReturn(table);
    when(table.indexes()).thenReturn(Collections.<Index>emptyList());
    when(from.getName()).thenReturn("old");
    when(to.getName()).thenReturn("new");
    ChangeIndex change = new ChangeIndex("sample", from, to);

    try {
      change.apply(schema);
    } catch (IllegalArgumentException expected) {
      assertEquals("Cannot change index [old] as it does not exist on table [sample]", expected.getMessage());
      return;
    }
    throw new AssertionError("Expected IllegalArgumentException");
  }

  @Test
  public void reverseReturnsSchemaWithRestoredIndex() {
    Index from = mock(Index.class);
    Index to = mock(Index.class);
    Table table = mock(Table.class);
    Schema schema = mock(Schema.class);
    when(schema.getTable("sample")).thenReturn(table);
    when(table.indexes()).thenReturn(Arrays.asList(to));
    when(from.getName()).thenReturn("old");
    when(to.getName()).thenReturn("new");
    ChangeIndex change = new ChangeIndex("sample", from, to);

    Schema result = change.reverse(schema);

    assertNotNull(result);
    assertTrue(result.tableExists("sample"));
  }
}
