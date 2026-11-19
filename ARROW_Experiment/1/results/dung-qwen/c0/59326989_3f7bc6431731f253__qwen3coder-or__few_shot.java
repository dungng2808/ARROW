package com.cloudera.labs.envelope.validate;

import com.cloudera.labs.envelope.utils.ConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MandatoryPathValidation_ArrowTest {

    @Test
    public void testValidateWithExistingPathAndMatchingType() {
        Config config = ConfigFactory.parseString("test.path = \"value\"");
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.STRING);
        
        ValidationResult result = validation.validate(config);
        
        assertEquals(Validity.VALID, result.getValidity());
        assertEquals("Mandatory configuration exists and with the required type: test.path", result.getMessage());
    }

    @Test
    public void testValidateWithExistingPathAndNonMatchingType() {
        Config config = ConfigFactory.parseString("test.path = 123");
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.STRING);
        
        ValidationResult result = validation.validate(config);
        
        assertEquals(Validity.INVALID, result.getValidity());
        assertEquals("Mandatory configuration 'test.path' does not have required type: STRING", result.getMessage());
    }

    @Test
    public void testValidateWithExistingPathAndNoTypeSpecified() {
        Config config = ConfigFactory.parseString("test.path = \"value\"");
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path");
        
        ValidationResult result = validation.validate(config);
        
        assertEquals(Validity.VALID, result.getValidity());
        assertEquals("Mandatory configuration exists: test.path", result.getMessage());
    }

    @Test
    public void testValidateWithMissingPath() {
        Config config = ConfigFactory.parseString("other.path = \"value\"");
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path");
        
        ValidationResult result = validation.validate(config);
        
        assertEquals(Validity.INVALID, result.getValidity());
        assertEquals("Mandatory configuration does not exist: test.path", result.getMessage());
    }

    @Test
    public void testToStringWithType() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.STRING);
        
        String result = validation.toString();
        
        assertEquals("Mandatory path validation that 'test.path' must exist and have type 'STRING'", result);
    }

    @Test
    public void testToStringWithoutType() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path");
        
        String result = validation.toString();
        
        assertEquals("Mandatory path validation that 'test.path' must exist", result);
    }

    @Test
    public void testGetKnownPaths() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path");
        java.util.Set<String> paths = validation.getKnownPaths();
        
        assertEquals(1, paths.size());
        assertEquals("test.path", paths.iterator().next());
    }
}
