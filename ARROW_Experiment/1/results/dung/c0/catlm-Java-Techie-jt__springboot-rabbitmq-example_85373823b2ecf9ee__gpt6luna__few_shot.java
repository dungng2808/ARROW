package com.javatechie.rabbitmq.demo;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SpringbootRabbitmqExampleApplication_ArrowTest {

    @Test
    public void applicationCanBeCreated() {
        SpringbootRabbitmqExampleApplication application =
                new SpringbootRabbitmqExampleApplication();
        assertNotNull(application);
    }
}
