package com.cloudera.labs.envelope.validate;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueType;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MandatoryPathValidation_ArrowTest {

  @Test
  public void validatesExistingPathWithoutRequiredType() {
    Config config = ConfigFactory.parseString("name = \"Alice\"");
    MandatoryPathValidation validation = new MandatoryPathValidation("name");

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.VALID, result.getValidity());
    assertEquals("Mandatory configuration exists: name", result.getMessage());
  }

  @Test
  public void rejectsMissingPath() {
    Config config = ConfigFactory.empty();
    MandatoryPathValidation validation = new MandatoryPathValidation("missing");

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.INVALID, result.getValidity());
    assertEquals("Mandatory configuration does not exist: missing", result.getMessage());
  }

  @Test
  public void validatesPathWithMatchingType() {
    Config config = ConfigFactory.parseString("count = 3");
    MandatoryPathValidation validation =
        new MandatoryPathValidation("count", ConfigValueType.NUMBER);

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.VALID, result.getValidity());
    assertEquals("Mandatory configuration exists and with the required type: count",
        result.getMessage());
  }

  @Test
  public void rejectsPathWithIncompatibleType() {
    Config config = ConfigFactory.parseString("enabled = true");
    MandatoryPathValidation validation =
        new MandatoryPathValidation("enabled", ConfigValueType.NUMBER);

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.INVALID, result.getValidity());
    assertEquals("Mandatory configuration 'enabled' does not have required type: NUMBER",
        result.getMessage());
  }

  @Test
  public void stringifiesValidationWithoutType() {
    MandatoryPathValidation validation = new MandatoryPathValidation("name");

    assertEquals("Mandatory path validation that 'name' must exist", validation.toString());
  }

  @Test
  public void stringifiesValidationWithType() {
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
