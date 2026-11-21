package com.javatechie.reactive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SpringReactiveMongoCrudApplication_ArrowTest {

    @Test
    public void applicationCanBeInstantiated() {
        SpringReactiveMongoCrudApplication application = new SpringReactiveMongoCrudApplication();
        assertNotNull(application);
    }
}
