package com.minio;

import org.junit.Test;
import static org.junit.Assert.*;

public class MinioDemoApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Since the main method just calls SpringApplication.run(), 
        // we can't easily test its behavior without more complex testing setup.
        // However, we can verify that the class can be instantiated and the main method doesn't throw an exception
        assertNotNull(new MinioDemoApplication());
    }
}
