package com.javatechie.rabbitmq.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SpringbootRabbitmqExampleApplication_ArrowTest {

    @Test
    public void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() ->
                SpringbootRabbitmqExampleApplication.main(new String[0]));
    }
}
