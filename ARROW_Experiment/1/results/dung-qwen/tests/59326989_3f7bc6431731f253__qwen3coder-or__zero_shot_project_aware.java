package com.cloudera.labs.envelope.validate;

import com.cloudera.labs.envelope.utils.ConfigUtils;
import com.google.common.collect.Sets;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueType;
import org.junit.Test;
import org.junit.Before;

import java.util.Set;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class MandatoryPathValidation_ArrowTest {

    private Config configWithInt;
    private Config configWithString;
    private Config configWithoutPath;
    
    @Before
    public void setUp() {
        configWithInt = ConfigFactory.parseString("test.path = 42");
        configWithString = ConfigFactory.parseString("test.path = \"hello\"");
        configWithoutPath = ConfigFactory.parseString("other.path = \"value\"");
    }

    @Test
    public void testValidateWithMatchingType() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.INT);
        ValidationResult result = validation.validate(configWithInt);
        
        assertThat(result.getValidity(), is(Validity.VALID));
        assertThat(result.getMessage(), 
            is("Mandatory configuration exists and with the required type: test.path"));
    }

    @Test
    public void testValidateWithNonMatchingType() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.STRING);
        ValidationResult result = validation.validate(configWithInt);
        
        assertThat(result.getValidity(), is(Validity.INVALID));
        assertThat(result.getMessage(), 
            is("Mandatory configuration 'test.path' does not have required type: STRING"));
    }

    @Test
    public void testValidateWithCoercibleType() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.STRING);
        ValidationResult result = validation.validate(configWithString);
        
        assertThat(result.getValidity(), is(Validity.VALID));
        assertThat(result.getMessage(), 
            is("Mandatory configuration exists and with the required type: test.path"));
    }

    @Test
    public void testValidateWithoutPath() {
        MandatoryPathValidation validation = new MandatoryPathValidation("missing.path");
        ValidationResult result = validation.validate(configWithInt);
        
        assertThat(result.getValidity(), is(Validity.INVALID));
        assertThat(result.getMessage(), 
            is("Mandatory configuration does not exist: missing.path"));
    }

    @Test
    public void testValidateWithoutTypeSpecified() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path");
        ValidationResult result = validation.validate(configWithInt);
        
        assertThat(result.getValidity(), is(Validity.VALID));
        assertThat(result.getMessage(), 
            is("Mandatory configuration exists: test.path"));
    }

    @Test
    public void testToStringWithType() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path", ConfigValueType.INT);
        String result = validation.toString();
        
        assertThat(result, is("Mandatory path validation that 'test.path' must exist and have type 'INT'"));
    }

    @Test
    public void testToStringWithoutType() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path");
        String result = validation.toString();
        
        assertThat(result, is("Mandatory path validation that 'test.path' must exist"));
    }

    @Test
    public void testGetKnownPaths() {
        MandatoryPathValidation validation = new MandatoryPathValidation("test.path");
        Set<String> result = validation.getKnownPaths();
        
        assertThat(result, is(Sets.newHashSet("test.path")));
    }
}
