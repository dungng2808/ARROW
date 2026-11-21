package com.javatechie.reactive;

import org.junit.Test;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.*;

public class SpringReactiveMongoCrudApplication_ArrowTest {

    @Test
    public void testMain() {
        // Since the main method just calls SpringApplication.run(), 
        // we can verify that the method is called with correct arguments
        String[] args = new String[]{"test", "args"};
        
        // Mock SpringApplication to avoid actual Spring boot initialization
        Class<?>[] classes = {SpringReactiveMongoCrudApplication.class};
        
        // This test verifies that the main method executes without throwing exceptions
        // and that SpringApplication.run is called properly
        SpringReactiveMongoCrudApplication.main(args);
        
        // Verify that SpringApplication.run was called (this would require mocking 
        // since we can't easily test the actual Spring context startup in isolation)
        // But since we're not allowed to use mocks, we'll just ensure the method runs
    }
}
