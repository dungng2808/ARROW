package me.tuine.minio;

import org.junit.Test;
import static org.junit.Assert.*;

public class MinioApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Testing that the main method can be called without throwing exceptions
        // This test verifies the basic functionality of the main method
        // Since SpringApplication.run() is a black box, we just ensure it doesn't throw
        try {
            MinioApplication.main(new String[]{});
            // If we reach here, the method executed successfully
            assertTrue(true);
        } catch (Exception e) {
            fail("Main method should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testStaticMainMethodExists() {
        // Verify that the main method exists and is properly declared
        try {
            java.lang.reflect.Method mainMethod = MinioApplication.class.getMethod("main", String[].class);
            assertNotNull(mainMethod);
            assertEquals("main", mainMethod.getName());
            assertTrue(Modifier.isPublic(mainMethod.getModifiers()));
            assertTrue(Modifier.isStatic(mainMethod.getModifiers()));
        } catch (NoSuchMethodException e) {
            fail("Main method should exist: " + e.getMessage());
        }
    }
}
