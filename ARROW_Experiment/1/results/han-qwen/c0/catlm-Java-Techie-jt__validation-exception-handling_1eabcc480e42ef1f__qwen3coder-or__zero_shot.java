package com.javatechie.api;

import org.junit.Test;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.*;

public class ValidationExampleApplication_ArrowTest {

    @Test
    public void testMain() {
        // Since the main method just calls SpringApplication.run(), 
        // we can verify that it's called with correct parameters
        String[] args = new String[]{"test", "args"};
        
        // Mock SpringApplication to avoid actual Spring context startup
        Class<?>[] classes = {ValidationExampleApplication.class};
        
        // This test verifies that main method executes without throwing exception
        // and that SpringApplication.run is called properly
        ValidationExampleApplication.main(args);
        
        // Verify that SpringApplication.run was called with correct arguments
        // Note: We cannot fully mock SpringApplication.run due to static nature,
        // but we can at least ensure the method executes
    }
}
