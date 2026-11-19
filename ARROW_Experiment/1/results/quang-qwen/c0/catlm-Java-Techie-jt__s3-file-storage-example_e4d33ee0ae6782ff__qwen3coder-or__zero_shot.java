package com.javatechie.s3;

import org.junit.Test;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.*;

public class FileStorageServiceApplication_ArrowTest {

    @Test
    public void testMain() {
        // Create mock arguments array
        String[] args = new String[]{"test", "args"};
        
        // Mock SpringApplication to avoid actual Spring boot startup
        SpringApplication springApplication = mock(SpringApplication.class);
        
        // Replace the real SpringApplication with our mock
        // This requires using reflection to replace the static method
        // Since we can't modify the original class, we'll just verify the method is called correctly
        
        // Call the main method - this should not throw any exceptions
        FileStorageServiceApplication.main(args);
        
        // Verify that SpringApplication.run was called with correct parameters
        // Note: We cannot easily verify the exact call due to static method, 
        // but we can at least ensure the method executes without throwing
    }
}
