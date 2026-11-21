package com.example.quartz;

import org.junit.Test;
import static org.junit.Assert.*;

public class SampleQuartzApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Testing that the main method can be called without throwing exceptions
        // This test verifies the basic functionality of the main method
        try {
            SampleQuartzApplication.main(new String[]{});
            // If we reach here, the main method executed successfully
            assertTrue(true);
        } catch (Exception e) {
            fail("Main method should not throw exception: " + e.getMessage());
        }
    }
}
