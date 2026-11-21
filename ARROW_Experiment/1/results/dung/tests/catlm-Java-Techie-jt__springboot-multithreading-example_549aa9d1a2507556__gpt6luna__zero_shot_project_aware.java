package com.javatechie.executor.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SpringbootMultithreadingExampleApplication_ArrowTest {

    @Test
    public void applicationCanBeInstantiated() {
        SpringbootMultithreadingExampleApplication application =
                new SpringbootMultithreadingExampleApplication();

        assertNotNull(application);
    }
}
