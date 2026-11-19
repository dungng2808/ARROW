package org.alfasoftware.morf.upgrade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfasoftware.morf.metadata.Index;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.metadata.Table;
import org.junit.Test;

public class ChangeIndex_ArrowTest {

  @Test
  public void gettersReturnConstructorValues() {
    Index from = new Index("OLD", Arrays.asList("A"));
    Index to = new Index("NEW", Arrays.asList("B"));
    ChangeIndex change = new ChangeIndex("MY_TABLE", from, to);

    assertEquals("MY_TABLE", change.getTableName());
    assertSame(from, change.getFromIndex());
    assertSame(to, change.getToIndex());
  }

  @Test
  public void toStringIncludesChangeDetails() {
    Index from = new Index("OLD", Arrays.asList("A"));
    Index to = new Index("NEW", Arrays.asList("B"));
    ChangeIndex change = new ChangeIndex("MY_TABLE", from, to);

    assertEquals("ChangeIndex [tableName=MY_TABLE, fromIndex=" + from + ", toIndex=" + to + "]", change.toString());
  }

  @Test
  public void applyRejectsNullStartingIndex() {
    ChangeIndex change = new ChangeIndex("MY_TABLE", null, new Index("NEW", Arrays.asList("A")));

    try {
      change.apply(null);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      assertEquals("Cannot change a null index to have a new definition", expected.getMessage());
    }
  }

  @Test
  public void applyRejectsNullEndingIndex() {
    Index from = new Index("OLD", Arrays.asList("A"));
    ChangeIndex change = new ChangeIndex("MY_TABLE", from, null);

    try {
      change.apply(null);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      assertEquals("Cannot change index [OLD] to be null", expected.getMessage());
    }
  }

  @Test
  public void isAppliedReturnsFalseWhenTableDoesNotExist() {
    Schema schema = new Schema() {
      @Override
      public boolean tableExists(String tableName) {
        return false;
      }

      @Override
      public Table getTable(String tableName) {
        throw new AssertionError("Table should not be requested");
      }
    };
    ChangeIndex change = new ChangeIndex("MISSING", new Index("OLD", Arrays.asList("A")),
        new Index("NEW", Arrays.asList("A")));

    assertFalse(change.isApplied(schema, null));
  }

  @Test
  public void acceptVisitsThisChange() {
    final ChangeIndex change = new ChangeIndex("MY_TABLE", new Index("OLD", Arrays.asList("A")),
        new Index("NEW", Arrays.asList("A")));
    final SchemaChange[] visited = new SchemaChange[1];
    SchemaChangeVisitor visitor = new SchemaChangeVisitor() {
      @Override
      public void visit(ChangeIndex actual) {
        visited[0] = actual;
      }
    };

    change.accept(visitor);

    assertSame(change, visited[0]);
  }
}
