package com.javatechie.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ValidationExampleApplication_ArrowTest {

    @Test
    public void applicationCanBeConstructed() {
        ValidationExampleApplication application = new ValidationExampleApplication();
        assertNotNull(application);
    }
}
