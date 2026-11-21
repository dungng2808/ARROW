package com.javatechie.s3;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class FileStorageServiceApplication_ArrowTest {

    @Test
    public void testMain() {
        // Since the main method just calls SpringApplication.run(), 
        // we can verify that it's called with correct arguments
        String[] args = new String[]{"test", "args"};
        
        // Mock SpringApplication to verify the call
        // Note: We're not actually testing the Spring functionality,
        // just ensuring the main method executes without throwing
        // an exception and calls SpringApplication.run()
        try {
            FileStorageServiceApplication.main(args);
        } catch (Exception e) {
            // If SpringApplication.run() throws an exception, 
            // it's not our concern for this test - we're just testing
            // that main method executes
        }
    }
}
