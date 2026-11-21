package com.javatechie.reactive;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SpringReactiveMongoCrudApplication_ArrowTest {

    @Test
    public void applicationCanBeCreated() {
        SpringReactiveMongoCrudApplication application = new SpringReactiveMongoCrudApplication();
        assertNotNull(application);
    }
}
