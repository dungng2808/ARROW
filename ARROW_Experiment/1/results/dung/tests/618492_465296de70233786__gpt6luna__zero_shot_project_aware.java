package site.ycsb;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertNotEquals;
import static org.testng.Assert.assertTrue;

public class Status_ArrowTest {

  @Test
  public void gettersReturnConstructorValues() {
    Status status = new Status("CUSTOM", "A custom status.");

    assertEquals(status.getName(), "CUSTOM");
    assertEquals(status.getDescription(), "A custom status.");
  }

  @Test
  public void gettersPreserveNullValues() {
    Status status = new Status(null, null);

    assertEquals(status.getName(), null);
    assertEquals(status.getDescription(), null);
  }

  @Test
  public void toStringFormatsStatus() {
    Status status = new Status("CUSTOM", "A custom status.");

    assertEquals(status.toString(), "Status [name=CUSTOM, description=A custom status.]");
  }

  @Test
  public void toStringFormatsNullValues() {
    Status status = new Status(null, null);

    assertEquals(status.toString(), "Status [name=null, description=null]");
  }

  @Test
  public void equalsRecognizesSameInstance() {
    Status status = new Status("CUSTOM", "A custom status.");

    assertTrue(status.equals(status));
  }

  @Test
  public void equalsRecognizesMatchingValues() {
    Status status = new Status("CUSTOM", "A custom status.");
    Status matching = new Status("CUSTOM", "A custom status.");

    assertTrue(status.equals(matching));
  }

  @Test
  public void equalsRejectsDifferentName() {
    Status status = new Status("CUSTOM", "A custom status.");
    Status different = new Status("OTHER", "A custom status.");

    assertFalse(status.equals(different));
  }

  @Test
  public void equalsRejectsDifferentDescription() {
    Status status = new Status("CUSTOM", "A custom status.");
    Status different = new Status("CUSTOM", "Another description.");

    assertFalse(status.equals(different));
  }

  @Test
  public void equalsHandlesNullFields() {
    Status bothNull = new Status(null, null);
    Status matching = new Status(null, null);
    Status differentName = new Status("CUSTOM", null);
    Status differentDescription = new Status(null, "Description");

    assertTrue(bothNull.equals(matching));
    assertFalse(bothNull.equals(differentName));
    assertFalse(bothNull.equals(differentDescription));
  }

  @Test
  public void equalsRejectsNullAndDifferentType() {
    Status status = new Status("CUSTOM", "A custom status.");

    assertFalse(status.equals(null));
    assertFalse(status.equals("CUSTOM"));
  }

  @Test
  public void hashCodeMatchesEqualStatus() {
    Status status = new Status("CUSTOM", "A custom status.");
    Status matching = new Status("CUSTOM", "A custom status.");

    assertEquals(status.hashCode(), matching.hashCode());
  }

  @Test
  public void hashCodeForNullFieldsIsOne() {
    Status status = new Status(null, null);

    assertEquals(status.hashCode(), 1);
  }

  @Test
  public void isOkForSuccessfulStatuses() {
    assertTrue(Status.OK.isOk());
    assertTrue(Status.BATCHED_OK.isOk());
  }

  @Test
  public void isOkForOtherStatuses() {
    assertFalse(Status.ERROR.isOk());
    assertFalse(new Status("OK", "The operation completed successfully.").isOk());
  }

  @Test
  public void constantsHaveExpectedValues() {
    assertEquals(Status.OK.getName(), "OK");
    assertEquals(Status.OK.getDescription(), "The operation completed successfully.");
    assertEquals(Status.ERROR.getName(), "ERROR");
    assertEquals(Status.ERROR.getDescription(), "The operation failed.");
    assertEquals(Status.NOT_FOUND.getName(), "NOT_FOUND");
    assertEquals(Status.NOT_FOUND.getDescription(), "The requested record was not found.");
    assertEquals(Status.NOT_IMPLEMENTED.getName(), "NOT_IMPLEMENTED");
    assertEquals(Status.NOT_IMPLEMENTED.getDescription(),
        "The operation is not implemented for the current binding.");
    assertEquals(Status.UNEXPECTED_STATE.getName(), "UNEXPECTED_STATE");
    assertEquals(Status.UNEXPECTED_STATE.getDescription(),
        "The operation reported success, but the result was not as expected.");
    assertEquals(Status.BAD_REQUEST.getName(), "BAD_REQUEST");
    assertEquals(Status.BAD_REQUEST.getDescription(), "The request was not valid.");
    assertEquals(Status.FORBIDDEN.getName(), "FORBIDDEN");
    assertEquals(Status.FORBIDDEN.getDescription(), "The operation is forbidden.");
    assertEquals(Status.SERVICE_UNAVAILABLE.getName(), "SERVICE_UNAVAILABLE");
    assertEquals(Status.SERVICE_UNAVAILABLE.getDescription(),
        "Dependant service for the current binding is not available.");
    assertEquals(Status.BATCHED_OK.getName(), "BATCHED_OK");
    assertEquals(Status.BATCHED_OK.getDescription(),
        "The operation has been batched by the binding to be executed later.");
    assertEquals(Status.TRANSACTION_FAILED.getName(), "TRANSACTION_FAILED");
    assertEquals(Status.TRANSACTION_FAILED.getDescription(),
        "The transaction was not successful due to integrity constraints, no error occured.");
  }
}
