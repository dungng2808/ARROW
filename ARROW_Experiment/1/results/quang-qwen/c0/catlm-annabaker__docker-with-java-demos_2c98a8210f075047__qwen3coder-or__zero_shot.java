package com.example.demo;

import org.junit.Test;
import static org.junit.Assert.*;

public class DemoApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Since the main method just calls SpringApplication.run(), 
        // we can't easily test its behavior without more complex setup.
        // However, we can verify that the method exists and is callable.
        try {
            DemoApplication.main(new String[]{});
            // If we reach here, the method executed without throwing an exception
            assertTrue(true);
        } catch (Exception e) {
            // This should not happen in normal circumstances
            fail("Main method threw exception: " + e.getMessage());
        }
    }
}
