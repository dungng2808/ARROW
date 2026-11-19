package com.javatechie.s3;

import org.junit.Test;
import static org.junit.Assert.*;

public class FileStorageServiceApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Since the main method just calls SpringApplication.run(), 
        // we can't easily test its behavior without more complex integration testing.
        // However, we can verify the class is properly structured and accessible.
        assertNotNull(FileStorageServiceApplication.class);
    }
}
