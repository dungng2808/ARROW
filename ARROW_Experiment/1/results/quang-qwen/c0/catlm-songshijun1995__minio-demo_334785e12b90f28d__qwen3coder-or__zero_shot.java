package com.minio;

import org.junit.Test;
import static org.junit.Assert.*;

public class MinioDemoApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Test that the main method can be called without throwing exceptions
        // This test verifies the basic functionality of the main method
        try {
            MinioDemoApplication.main(new String[]{});
            // If we reach here, the main method executed successfully
            assertTrue(true);
        } catch (Exception e) {
            fail("Main method should not throw exception: " + e.getMessage());
        }
    }
}
