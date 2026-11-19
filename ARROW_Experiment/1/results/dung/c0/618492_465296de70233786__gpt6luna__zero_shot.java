package site.ycsb;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Status_ArrowTest {

  @Test
  public void gettersReturnConstructorValues() {
    Status status = new Status("CUSTOM", "A custom status.");
    assertEquals("CUSTOM", status.getName());
    assertEquals("A custom status.", status.getDescription());
  }

  @Test
  public void gettersPreserveNullValues() {
    Status status = new Status(null, null);
    assertEquals(null, status.getName());
    assertEquals(null, status.getDescription());
  }

  @Test
  public void toStringFormatsValues() {
    Status status = new Status("CUSTOM", "A custom status.");
    assertEquals("Status [name=CUSTOM, description=A custom status.]", status.toString());
  }

  @Test
  public void toStringFormatsNullValues() {
    Status status = new Status(null, null);
    assertEquals("Status [name=null, description=null]", status.toString());
  }

  @Test
  public void equalStatusesHaveEqualHashCodes() {
    Status first = new Status("CUSTOM", "A custom status.");
    Status second = new Status("CUSTOM", "A custom status.");
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  public void equalsHandlesIdentityNullAndDifferentTypes() {
    Status status = new Status("CUSTOM", "A custom status.");
    assertTrue(status.equals(status));
    assertFalse(status.equals(null));
    assertFalse(status.equals("CUSTOM"));
  }

  @Test
  public void equalsComparesNameAndDescription() {
    Status status = new Status("CUSTOM", "A custom status.");
    assertNotEquals(status, new Status("OTHER", "A custom status."));
    assertNotEquals(status, new Status("CUSTOM", "Another description."));
  }

  @Test
  public void equalsHandlesNullFields() {
    Status bothNull = new Status(null, null);
    assertEquals(bothNull, new Status(null, null));
    assertNotEquals(bothNull, new Status("CUSTOM", null));
    assertNotEquals(bothNull, new Status(null, "A description."));
  }

  @Test
  public void hashCodeHandlesNullFields() {
    Status status = new Status(null, null);
    assertEquals(961, status.hashCode());
  }

  @Test
  public void okIsPassing() {
    assertTrue(Status.OK.isOk());
  }

  @Test
  public void batchedOkIsPassing() {
    assertTrue(Status.BATCHED_OK.isOk());
  }

  @Test
  public void otherStatusesAreNotPassing() {
    assertFalse(Status.ERROR.isOk());
    assertFalse(Status.NOT_FOUND.isOk());
    assertFalse(Status.NOT_IMPLEMENTED.isOk());
    assertFalse(Status.UNEXPECTED_STATE.isOk());
    assertFalse(Status.BAD_REQUEST.isOk());
    assertFalse(Status.FORBIDDEN.isOk());
    assertFalse(Status.SERVICE_UNAVAILABLE.isOk());
    assertFalse(Status.TRANSACTION_FAILED.isOk());
  }

  @Test
  public void customStatusWithOkValuesIsNotPassing() {
    Status custom = new Status("OK", "The operation completed successfully.");
    assertFalse(custom.isOk());
  }

  @Test
  public void constantsHaveExpectedValues() {
    assertEquals("OK", Status.OK.getName());
    assertEquals("The operation completed successfully.", Status.OK.getDescription());
    assertEquals("ERROR", Status.ERROR.getName());
    assertEquals("The operation failed.", Status.ERROR.getDescription());
    assertEquals("NOT_FOUND", Status.NOT_FOUND.getName());
    assertEquals("The requested record was not found.", Status.NOT_FOUND.getDescription());
    assertEquals("NOT_IMPLEMENTED", Status.NOT_IMPLEMENTED.getName());
    assertEquals("The operation is not implemented for the current binding.",
        Status.NOT_IMPLEMENTED.getDescription());
    assertEquals("UNEXPECTED_STATE", Status.UNEXPECTED_STATE.getName());
    assertEquals("The operation reported success, but the result was not as expected.",
        Status.UNEXPECTED_STATE.getDescription());
    assertEquals("BAD_REQUEST", Status.BAD_REQUEST.getName());
    assertEquals("The request was not valid.", Status.BAD_REQUEST.getDescription());
    assertEquals("FORBIDDEN", Status.FORBIDDEN.getName());
    assertEquals("The operation is forbidden.", Status.FORBIDDEN.getDescription());
    assertEquals("SERVICE_UNAVAILABLE", Status.SERVICE_UNAVAILABLE.getName());
    assertEquals("Dependant service for the current binding is not available.",
        Status.SERVICE_UNAVAILABLE.getDescription());
    assertEquals("BATCHED_OK", Status.BATCHED_OK.getName());
    assertEquals("The operation has been batched by the binding to be executed later.",
        Status.BATCHED_OK.getDescription());
    assertEquals("TRANSACTION_FAILED", Status.TRANSACTION_FAILED.getName());
    assertEquals("The transaction was not successful due to integrity constraints, no error occured.",
        Status.TRANSACTION_FAILED.getDescription());
  }

  @Test
  public void constantsAreNonNull() {
    assertNotNull(Status.OK);
    assertNotNull(Status.ERROR);
    assertNotNull(Status.NOT_FOUND);
    assertNotNull(Status.NOT_IMPLEMENTED);
    assertNotNull(Status.UNEXPECTED_STATE);
    assertNotNull(Status.BAD_REQUEST);
    assertNotNull(Status.FORBIDDEN);
    assertNotNull(Status.SERVICE_UNAVAILABLE);
    assertNotNull(Status.BATCHED_OK);
    assertNotNull(Status.TRANSACTION_FAILED);
  }

  @Test
  public void constantsAreDistinctInstances() {
    assertNotEquals(Status.OK, Status.BATCHED_OK);
    assertSame(Status.OK, Status.OK);
  }
}
