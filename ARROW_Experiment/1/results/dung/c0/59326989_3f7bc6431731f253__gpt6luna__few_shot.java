package com.cloudera.labs.envelope.validate;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MandatoryPathValidation_ArrowTest {

  @Test
  public void validatesExistingPathWithoutRequiredType() {
    Config config = ConfigFactory.parseString("name = \"Alice\"");
    MandatoryPathValidation validation = new MandatoryPathValidation("name");

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.VALID, result.getValidity());
  }

  @Test
  public void validatesExistingPathWithMatchingType() {
    Config config = ConfigFactory.parseString("enabled = true");
    MandatoryPathValidation validation =
        new MandatoryPathValidation("enabled", ConfigValueType.BOOLEAN);

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.VALID, result.getValidity());
  }

  @Test
  public void validatesCoerciblePathType() {
    Config config = ConfigFactory.parseString("count = \"12\"");
    MandatoryPathValidation validation =
        new MandatoryPathValidation("count", ConfigValueType.NUMBER);

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.VALID, result.getValidity());
  }

  @Test
  public void rejectsPathWithWrongType() {
    Config config = ConfigFactory.parseString("enabled = true");
    MandatoryPathValidation validation =
        new MandatoryPathValidation("enabled", ConfigValueType.STRING);

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.INVALID, result.getValidity());
  }

  @Test
  public void rejectsMissingPath() {
    Config config = ConfigFactory.empty();
    MandatoryPathValidation validation = new MandatoryPathValidation("missing");

    ValidationResult result = validation.validate(config);

    assertEquals(Validity.INVALID, result.getValidity());
  }

  @Test
  public void returnsKnownPath() {
    MandatoryPathValidation validation = new MandatoryPathValidation("service.name");

    Set<String> expected = new HashSet<>();
    expected.add("service.name");

    assertEquals(expected, validation.getKnownPaths());
  }

  @Test
  public void describesPathWithoutType() {
    MandatoryPathValidation validation = new MandatoryPathValidation("service.name");

    assertEquals("Mandatory path validation that 'service.name' must exist",
        validation.toString());
  }

  @Test
  public void describesPathWithType() {
    MandatoryPathValidation validation =
        new MandatoryPathValidation("enabled", ConfigValueType.BOOLEAN);

    assertEquals("Mandatory path validation that 'enabled' must exist and have type 'BOOLEAN'",
        validation.toString());
  }
}
