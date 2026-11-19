package com.example.demo;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class DemoApplication_ArrowTest {

    @Test
    public void testMain() {
        // This test verifies that the main method can be called without throwing exceptions
        // Since the main method just calls SpringApplication.run(), we test that it doesn't throw
        // We don't need to verify anything specific since it's a standard Spring Boot entry point
        DemoApplication.main(new String[]{});
    }
}
