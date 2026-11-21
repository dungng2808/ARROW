package com.javatechie.api;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ValidationExampleApplication_ArrowTest {

    @Test
    public void canCreateApplication() {
        ValidationExampleApplication application = new ValidationExampleApplication();
        assertNotNull(application);
    }
}
