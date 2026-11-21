package site.ycsb;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Status_ArrowTest {

  @Test
  public void testConstructor() {
    Status status = new Status("TEST", "Test description");
    Assert.assertEquals(status.getName(), "TEST");
    Assert.assertEquals(status.getDescription(), "Test description");
  }

  @Test
  public void testGetName() {
    Status status = new Status("NAME", "Description");
    Assert.assertEquals(status.getName(), "NAME");
  }

  @Test
  public void testGetDescription() {
    Status status = new Status("NAME", "Description");
    Assert.assertEquals(status.getDescription(), "Description");
  }

  @Test
  public void testToString() {
    Status status = new Status("TEST", "Test description");
    String expected = "Status [name=TEST, description=Test description]";
    Assert.assertEquals(status.toString(), expected);
  }

  @Test
  public void testHashCode() {
    Status status1 = new Status("TEST", "Test description");
    Status status2 = new Status("TEST", "Test description");
    Assert.assertEquals(status1.hashCode(), status2.hashCode());
  }

  @Test
  public void testEquals() {
    Status status1 = new Status("TEST", "Test description");
    Status status2 = new Status("TEST", "Test description");
    Assert.assertTrue(status1.equals(status2));
  }

  @Test
  public void testEqualsNull() {
    Status status = new Status("TEST", "Test description");
    Assert.assertFalse(status.equals(null));
  }

  @Test
  public void testEqualsDifferentClass() {
    Status status = new Status("TEST", "Test description");
    Assert.assertFalse(status.equals("not a status"));
  }

  @Test
  public void testEqualsSameObject() {
    Status status = new Status("TEST", "Test description");
    Assert.assertTrue(status.equals(status));
  }

  @Test
  public void testIsOkTrue() {
    Assert.assertTrue(Status.OK.isOk());
  }

  @Test
  public void testIsOkFalse() {
    Assert.assertFalse(Status.ERROR.isOk());
  }

  @Test
  public void testIsOkBatchedOk() {
    Assert.assertTrue(Status.BATCHED_OK.isOk());
  }

  @Test
  public void testStaticFields() {
    Assert.assertEquals(Status.OK.getName(), "OK");
    Assert.assertEquals(Status.OK.getDescription(), "The operation completed successfully.");

    Assert.assertEquals(Status.ERROR.getName(), "ERROR");
    Assert.assertEquals(Status.ERROR.getDescription(), "The operation failed.");

    Assert.assertEquals(Status.NOT_FOUND.getName(), "NOT_FOUND");
    Assert.assertEquals(Status.NOT_FOUND.getDescription(), "The requested record was not found.");

    Assert.assertEquals(Status.NOT_IMPLEMENTED.getName(), "NOT_IMPLEMENTED");
    Assert.assertEquals(Status.NOT_IMPLEMENTED.getDescription(), "The operation is not implemented for the current binding.");

    Assert.assertEquals(Status.UNEXPECTED_STATE.getName(), "UNEXPECTED_STATE");
    Assert.assertEquals(Status.UNEXPECTED_STATE.getDescription(), "The operation reported success, but the result was not as expected.");

    Assert.assertEquals(Status.BAD_REQUEST.getName(), "BAD_REQUEST");
    Assert.assertEquals(Status.BAD_REQUEST.getDescription(), "The request was not valid.");

    Assert.assertEquals(Status.FORBIDDEN.getName(), "FORBIDDEN");
    Assert.assertEquals(Status.FORBIDDEN.getDescription(), "The operation is forbidden.");

    Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getName(), "SERVICE_UNAVAILABLE");
    Assert.assertEquals(Status.SERVICE_UNAVAILABLE.getDescription(), "Dependant service for the current binding is not available.");

    Assert.assertEquals(Status.BATCHED_OK.getName(), "BATCHED_OK");
    Assert.assertEquals(Status.BATCHED_OK.getDescription(), "The operation has been batched by the binding to be executed later.");

    Assert.assertEquals(Status.TRANSACTION_FAILED.getName(), "TRANSACTION_FAILED");
    Assert.assertEquals(Status.TRANSACTION_FAILED.getDescription(), "The transaction was not successful due to integrity constraints, no error occured.");
  }
}
