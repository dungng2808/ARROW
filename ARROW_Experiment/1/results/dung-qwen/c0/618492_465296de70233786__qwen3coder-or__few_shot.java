package site.ycsb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Status_ArrowTest {

  @Test
  public void testStatusConstructor() {
    Status status = new Status("TEST", "Test description");
    assertEquals("TEST", status.getName());
    assertEquals("Test description", status.getDescription());
  }

  @Test
  public void testToString() {
    Status status = new Status("TEST", "Test description");
    String expected = "Status [name=TEST, description=Test description]";
    assertEquals(expected, status.toString());
  }

  @Test
  public void testHashCode() {
    Status status1 = new Status("TEST", "Test description");
    Status status2 = new Status("TEST", "Test description");
    assertEquals(status1.hashCode(), status2.hashCode());
  }

  @Test
  public void testEquals() {
    Status status1 = new Status("TEST", "Test description");
    Status status2 = new Status("TEST", "Test description");
    Status status3 = new Status("OTHER", "Other description");
    
    assertEquals(status1, status2);
    assertNotEquals(status1, status3);
    assertNotEquals(status1, null);
    assertNotEquals(status1, new Object());
  }

  @Test
  public void testIsOkWithOK() {
    assertTrue(Status.OK.isOk());
  }

  @Test
  public void testIsOkWithBATCHED_OK() {
    assertTrue(Status.BATCHED_OK.isOk());
  }

  @Test
  public void testIsOkWithOtherStatuses() {
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
  public void testStaticFields() {
    assertEquals("OK", Status.OK.getName());
    assertEquals("The operation completed successfully.", Status.OK.getDescription());
    
    assertEquals("ERROR", Status.ERROR.getName());
    assertEquals("The operation failed.", Status.ERROR.getDescription());
    
    assertEquals("NOT_FOUND", Status.NOT_FOUND.getName());
    assertEquals("The requested record was not found.", Status.NOT_FOUND.getDescription());
    
    assertEquals("NOT_IMPLEMENTED", Status.NOT_IMPLEMENTED.getName());
    assertEquals("The operation is not implemented for the current binding.", Status.NOT_IMPLEMENTED.getDescription());
    
    assertEquals("UNEXPECTED_STATE", Status.UNEXPECTED_STATE.getName());
    assertEquals("The operation reported success, but the result was not as expected.", Status.UNEXPECTED_STATE.getDescription());
    
    assertEquals("BAD_REQUEST", Status.BAD_REQUEST.getName());
    assertEquals("The request was not valid.", Status.BAD_REQUEST.getDescription());
    
    assertEquals("FORBIDDEN", Status.FORBIDDEN.getName());
    assertEquals("The operation is forbidden.", Status.FORBIDDEN.getDescription());
    
    assertEquals("SERVICE_UNAVAILABLE", Status.SERVICE_UNAVAILABLE.getName());
    assertEquals("Dependant service for the current binding is not available.", Status.SERVICE_UNAVAILABLE.getDescription());
    
    assertEquals("BATCHED_OK", Status.BATCHED_OK.getName());
    assertEquals("The operation has been batched by the binding to be executed later.", Status.BATCHED_OK.getDescription());
    
    assertEquals("TRANSACTION_FAILED", Status.TRANSACTION_FAILED.getName());
    assertEquals("The transaction was not successful due to integrity constraints, no error occured.", Status.TRANSACTION_FAILED.getDescription());
  }
}
