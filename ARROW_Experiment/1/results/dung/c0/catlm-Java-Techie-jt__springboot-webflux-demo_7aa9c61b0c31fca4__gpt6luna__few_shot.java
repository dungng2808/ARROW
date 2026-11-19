package com.javatechie.webflux;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SpringbootWebfluxDemoApplication_ArrowTest {

    @Test
    public void applicationCanBeCreated() {
        SpringbootWebfluxDemoApplication application = new SpringbootWebfluxDemoApplication();
        assertNotNull(application);
    }
}
