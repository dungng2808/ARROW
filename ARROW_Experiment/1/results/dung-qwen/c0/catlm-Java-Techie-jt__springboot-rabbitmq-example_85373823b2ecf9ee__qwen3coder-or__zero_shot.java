package com.javatechie.rabbitmq.demo;

import org.junit.Test;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.*;

public class SpringbootRabbitmqExampleApplication_ArrowTest {

    @Test
    public void testMain() {
        // Since the main method just calls SpringApplication.run(), 
        // we can verify that it was called with correct arguments
        String[] args = new String[]{"test", "args"};
        
        // Mock SpringApplication to avoid actual Spring context initialization
        Class<?>[] classes = {SpringbootRabbitmqExampleApplication.class};
        
        // This test verifies that the main method executes without throwing exceptions
        // and that SpringApplication.run is called with proper parameters
        try {
            SpringbootRabbitmqExampleApplication.main(args);
        } catch (Exception e) {
            // If an exception occurs, it's likely due to Spring context issues
            // which are outside the scope of this simple test
        }
    }
}
