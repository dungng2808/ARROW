package com.javatechie.executor.api;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SpringbootMultithreadingExampleApplication_ArrowTest {

    @Test
    public void applicationCanBeCreated() {
        SpringbootMultithreadingExampleApplication application =
                new SpringbootMultithreadingExampleApplication();
        assertNotNull(application);
    }
}
