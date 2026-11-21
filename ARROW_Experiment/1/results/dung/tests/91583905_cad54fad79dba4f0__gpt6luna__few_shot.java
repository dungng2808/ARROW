package org.alfasoftware.morf.upgrade;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.metadata.Index;
import org.alfasoftware.morf.metadata.Schema;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ChangeIndex_ArrowTest {

  @Test
  public void exposesConstructorValues() {
    Index from = new Index("OLD_INDEX");
    Index to = new Index("NEW_INDEX");
    ChangeIndex change = new ChangeIndex("SAMPLE_TABLE", from, to);

    assertEquals("SAMPLE_TABLE", change.getTableName());
    assertSame(from, change.getFromIndex());
    assertSame(to, change.getToIndex());
    assertEquals("ChangeIndex [tableName=SAMPLE_TABLE, fromIndex=" + from + ", toIndex=" + to + "]", change.toString());
  }

  @Test
  public void applyRejectsNullStartingIndex() {
    ChangeIndex change = new ChangeIndex("SAMPLE_TABLE", null, new Index("NEW_INDEX"));

    try {
      change.apply(null);
    } catch (IllegalStateException expected) {
      assertEquals("Cannot change a null index to have a new definition", expected.getMessage());
      return;
    }

    throw new AssertionError("Expected IllegalStateException");
  }

  @Test
  public void applyRejectsNullEndingIndex() {
    ChangeIndex change = new ChangeIndex("SAMPLE_TABLE", new Index("OLD_INDEX"), null);

    try {
      change.apply(null);
    } catch (IllegalStateException expected) {
      assertEquals("Cannot change index [OLD_INDEX] to be null", expected.getMessage());
      return;
    }

    throw new AssertionError("Expected IllegalStateException");
  }

  @Test
  public void isAppliedReturnsFalseWhenTableIsMissing() {
    Schema schema = new Schema();
    ChangeIndex change = new ChangeIndex("MISSING_TABLE", new Index("OLD_INDEX"), new Index("NEW_INDEX"));

    assertFalse(change.isApplied(schema, (ConnectionResources) null));
  }

  @Test
  public void applyRejectsMissingStartingIndex() {
    Schema schema = new Schema();
    ChangeIndex change = new ChangeIndex("SAMPLE_TABLE", new Index("OLD_INDEX"), new Index("NEW_INDEX"));

    try {
      change.apply(schema);
    } catch (RuntimeException expected) {
      assertNotNull(expected);
      return;
    }

    throw new AssertionError("Expected an exception");
  }

  @Test
  public void reverseRejectsNullStartingIndex() {
    ChangeIndex change = new ChangeIndex("SAMPLE_TABLE", new Index("OLD_INDEX"), null);

    try {
      change.reverse(null);
    } catch (IllegalStateException expected) {
      assertEquals("Cannot change a null index to have a new definition", expected.getMessage());
      return;
    }

    throw new AssertionError("Expected IllegalStateException");
  }
}
