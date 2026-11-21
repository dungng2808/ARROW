package com.javatechie.api;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class ValidationExampleApplication_ArrowTest {

    @Test
    public void testMain() {
        // Since the main method just calls SpringApplication.run(), 
        // we can verify that it's called with correct arguments
        String[] args = new String[]{"test", "args"};
        
        // Mock SpringApplication to verify the call
        try (var mockedStatic = mockStatic(SpringApplication.class)) {
            ValidationExampleApplication.main(args);
            
            // Verify that SpringApplication.run was called with correct parameters
            mockedStatic.verify(() -> SpringApplication.run(
                eq(ValidationExampleApplication.class), 
                eq(args)
            ));
        }
    }
}
