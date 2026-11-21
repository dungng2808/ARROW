package com.example;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ConditionalValidationApplication_ArrowTest {

    @Test
    public void canCreateApplication() {
        ConditionalValidationApplication application = new ConditionalValidationApplication();
        assertNotNull(application);
    }
}
