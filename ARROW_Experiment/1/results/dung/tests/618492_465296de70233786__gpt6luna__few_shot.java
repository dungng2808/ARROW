package site.ycsb;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class Status_ArrowTest {

  @Test
  public void gettersAndToStringReturnExpectedValues() {
    Status status = new Status("CUSTOM", "A custom status.");

    assertEquals("CUSTOM", status.getName());
    assertEquals("A custom status.", status.getDescription());
    assertEquals("Status [name=CUSTOM, description=A custom status.]", status.toString());
  }

  @Test
  public void nullValuesArePreserved() {
    Status status = new Status(null, null);

    assertEquals(null, status.getName());
    assertEquals(null, status.getDescription());
    assertEquals("Status [name=null, description=null]", status.toString());
  }

  @Test
  public void equalStatusesHaveEqualHashCodes() {
    Status first = new Status("CUSTOM", "A custom status.");
    Status second = new Status("CUSTOM", "A custom status.");

    assertTrue(first.equals(second));
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  public void statusesWithDifferentFieldsAreNotEqual() {
    Status original = new Status("CUSTOM", "A custom status.");

    assertFalse(original.equals(new Status("OTHER", "A custom status.")));
    assertFalse(original.equals(new Status("CUSTOM", "A different status.")));
    assertFalse(original.equals(null));
    assertFalse(original.equals("CUSTOM"));
    assertTrue(original.equals(original));
  }

  @Test
  public void nullFieldsAreComparedCorrectly() {
    Status bothNull = new Status(null, null);
    Status sameNulls = new Status(null, null);

    assertTrue(bothNull.equals(sameNulls));
    assertFalse(bothNull.equals(new Status("CUSTOM", null)));
    assertFalse(bothNull.equals(new Status(null, "description")));
  }

  @Test
  public void onlyOkStatusesArePassing() {
    assertTrue(Status.OK.isOk());
    assertTrue(Status.BATCHED_OK.isOk());
    assertFalse(Status.ERROR.isOk());
    assertFalse(new Status("OK", "The operation completed successfully.").isOk());
  }
}
