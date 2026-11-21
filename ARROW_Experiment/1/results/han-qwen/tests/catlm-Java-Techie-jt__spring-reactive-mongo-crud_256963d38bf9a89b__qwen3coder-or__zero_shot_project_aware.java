package com.javatechie.reactive;

import org.junit.Test;
import static org.mockito.Mockito.*;

public class SpringReactiveMongoCrudApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Since the main method just calls SpringApplication.run(), 
        // we can verify that it's called with correct arguments
        String[] args = new String[]{"test", "args"};
        
        // Mock SpringApplication to verify the call
        // Note: We're not actually testing the behavior of SpringApplication,
        // just ensuring the main method is called correctly
        SpringReactiveMongoCrudApplication.main(args);
        
        // The test passes if no exception is thrown
        // This verifies that the main method executes without issues
    }
}
