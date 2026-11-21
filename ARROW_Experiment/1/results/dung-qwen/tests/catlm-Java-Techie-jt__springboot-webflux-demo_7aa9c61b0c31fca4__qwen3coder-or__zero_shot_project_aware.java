package com.javatechie.webflux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.mockito.Mockito.*;

public class SpringbootWebfluxDemoApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Arrange
        String[] args = new String[]{"test", "args"};
        
        // Act
        SpringbootWebfluxDemoApplication.main(args);
        
        // Assert - Verify that SpringApplication.run was called with correct arguments
        verify(SpringApplication.class);
        SpringApplication.run(SpringbootWebfluxDemoApplication.class, args);
    }
}
