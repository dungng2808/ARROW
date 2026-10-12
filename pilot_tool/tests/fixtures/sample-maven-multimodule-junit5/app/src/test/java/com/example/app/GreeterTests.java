package com.example.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GreeterTests {

    @Test
    void greets() {
        assertEquals("Hello, Bob!", new Greeter("Hello").greet("bob"));
    }
}
