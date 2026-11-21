package com.cloudera.labs.envelope.validate;

import com.cloudera.labs.envelope.utils.ConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueType;
import org.junit.Test;

import static org.junit.Assert.*;

public class MandatoryPathValidation_ArrowTest {

    @Test
    public void testValidateWithMatchingType() {
        Config config = ConfigFactory.parseString("test.path = \"value\"");
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.STRING);
        ValidationResult result = validation.validate(config);
        
        assertEquals(Validity.VALID, result.getValidity());
        assertTrue(result.getMessage().contains("Mandatory configuration exists and with the required type"));
    }

    @Test
    public void testValidateWithNonMatchingType() {
        Config config = ConfigFactory.parseString("test.path = 123");
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.STRING);
        ValidationResult result = validation.validate(config);
        
        assertEquals(Validity.INVALID, result.getValidity());
        assertTrue(result.getMessage().contains("does not have required type"));
    }

    @Test
    public void testValidateWithCoercibleType() {
        Config config = ConfigFactory.parseString("test.path = 123");
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.INT);
        ValidationResult result = validation.validate(config);
        
        assertEquals(Validity.VALID, result.getValidity());
        assertTrue(result.getMessage().contains("Mandatory configuration exists and with the required type"));
    }

    @Test
    public void testValidateWithoutTypeCheck() {
        Config config = ConfigFactory.parseString("test.path = \"value\"");
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path");
        ValidationResult result = validation.validate(config);
        
        assertEquals(Validity.VALID, result.getValidity());
        assertTrue(result.getMessage().contains("Mandatory configuration exists"));
    }

    @Test
    public void testValidateMissingPath() {
        Config config = ConfigFactory.parseString("other.path = \"value\"");
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.STRING);
        ValidationResult result = validation.validate(config);
        
        assertEquals(Validity.INVALID, result.getValidity());
        assertTrue(result.getMessage().contains("does not exist"));
    }

    @Test
    public void testToStringWithType() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.STRING);
        String result = validation.toString();
        
        assertTrue(result.contains("Mandatory path validation that 'test.path' must exist and have type 'STRING'"));
    }

    @Test
    public void testToStringWithoutType() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path");
        String result = validation.toString();
        
        assertTrue(result.contains("Mandatory path validation that 'test.path' must exist"));
    }

    @Test
    public void testGetKnownPaths() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.STRING);
        java.util.Set<String> paths = validation.getKnownPaths();
        
        assertEquals(1, paths.size());
        assertTrue(paths.contains("test.path"));
    }
}
