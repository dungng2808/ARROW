package com.javatechie.api;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ValidationExampleApplication_ArrowTest {

    @Test
    public void applicationCanBeInstantiated() {
        ValidationExampleApplication application = new ValidationExampleApplication();
        assertNotNull(application);
    }
}
