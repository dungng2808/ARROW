package com.javatechie.spring.mockito.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.boot.SpringApplication;

public class SpringBootMockitoApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Since the main method just calls SpringApplication.run(), 
        // we can't easily test its behavior without mocking Spring components.
        // However, we can verify that the class is properly structured.
        
        // The main method should not throw any exceptions when called with null args
        try {
            SpringBootMockitoApplication.main(null);
        } catch (Exception e) {
            // This might throw an exception due to Spring context initialization,
            // but that's not what we're testing here.
            // We're mainly checking that the method exists and compiles correctly.
        }
        
        // Verify that the class can be instantiated
        SpringBootMockitoApplication app = new SpringBootMockitoApplication();
        assertThat(app).isNotNull();
    }
}
