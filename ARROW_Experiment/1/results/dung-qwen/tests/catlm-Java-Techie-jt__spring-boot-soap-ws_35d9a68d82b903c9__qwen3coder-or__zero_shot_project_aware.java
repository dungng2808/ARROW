package com.javatechie.spring.soap.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.springframework.boot.SpringApplication;

public class SpringBootSopaWsApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Arrange
        String[] args = new String[]{"test", "args"};
        
        // Act
        SpringBootSopaWsApplication.main(args);
        
        // Verify that SpringApplication.run was called with correct arguments
        // Note: We cannot directly verify the call since main method uses static
        // but we can at least ensure the method executes without throwing
    }
}
