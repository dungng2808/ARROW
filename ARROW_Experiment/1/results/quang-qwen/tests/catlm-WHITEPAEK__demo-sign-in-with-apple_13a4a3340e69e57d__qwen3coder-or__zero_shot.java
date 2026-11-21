package com.whitepaek.demosigninwithapple;

import org.junit.Test;
import static org.junit.Assert.*;

public class Application_ArrowTest {

    @Test
    public void testMainMethod() {
        // Since the main method just calls SpringApplication.run(), 
        // we can't easily test its behavior without more complex integration testing.
        // However, we can verify that the method exists and is callable.
        try {
            Application.main(new String[]{});
            // If we reach here, the method executed without throwing an exception
            assertTrue(true);
        } catch (Exception e) {
            fail("main method should not throw exception: " + e.getMessage());
        }
    }
}
