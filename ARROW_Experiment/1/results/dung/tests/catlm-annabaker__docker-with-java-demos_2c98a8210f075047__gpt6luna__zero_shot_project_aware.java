package com.example.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DemoApplication_ArrowTest {

    @Test
    public void mainRunsWithoutArguments() {
        assertDoesNotThrow(() -> DemoApplication.main(new String[0]));
    }
}
