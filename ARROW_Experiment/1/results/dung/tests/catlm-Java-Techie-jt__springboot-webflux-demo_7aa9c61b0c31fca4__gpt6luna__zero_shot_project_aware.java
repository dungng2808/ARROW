package com.javatechie.webflux;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SpringbootWebfluxDemoApplication_ArrowTest {

    @Test
    public void applicationCanBeInstantiated() {
        assertDoesNotThrow(SpringbootWebfluxDemoApplication::new);
    }
}
