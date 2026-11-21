package com.cloudera.labs.envelope.validate;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueType;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MandatoryPathValidation_ArrowTest {

  @Test
  public void validatesExistingPathWithoutRequiredType() {
    MandatoryPathValidation validation = new MandatoryPathValidation("name");
    Config config = ConfigFactory.parseString("name = \"Alice\"");

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.VALID, result.getValidity());
    assertEquals("Mandatory configuration exists: name", result.getMessage());
  }

  @Test
  public void rejectsMissingPath() {
    MandatoryPathValidation validation = new MandatoryPathValidation("name");
    Config config = ConfigFactory.empty();

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.INVALID, result.getValidity());
    assertEquals("Mandatory configuration does not exist: name", result.getMessage());
  }

  @Test
  public void validatesPathWithRequiredType() {
    MandatoryPathValidation validation =
        new MandatoryPathValidation("count", ConfigValueType.NUMBER);
    Config config = ConfigFactory.parseString("count = 3");

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.VALID, result.getValidity());
    assertEquals("Mandatory configuration exists and with the required type: count", result.getMessage());
  }

  @Test
  public void rejectsPathWithWrongRequiredType() {
    MandatoryPathValidation validation =
        new MandatoryPathValidation("name", ConfigValueType.NUMBER);
    Config config = ConfigFactory.parseString("name = \"Alice\"");

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.INVALID, result.getValidity());
    assertEquals("Mandatory configuration 'name' does not have required type: NUMBER", result.getMessage());
  }

  @Test
  public void toStringWithoutRequiredType() {
    MandatoryPathValidation validation = new MandatoryPathValidation("name");

    assertEquals("Mandatory path validation that 'name' must exist", validation.toString());
  }

  @Test
  public void toStringWithRequiredType() {
    MandatoryPathValidation validation =
        new MandatoryPathValidation("count", ConfigValueType.NUMBER);

    assertEquals("Mandatory path validation that 'count' must exist and have type 'NUMBER'",
        validation.toString());
  }

  @Test
  public void returnsKnownPath() {
    MandatoryPathValidation validation = new MandatoryPathValidation("name");

    assertEquals(Collections.singleton("name"), validation.getKnownPaths());
  }
}
