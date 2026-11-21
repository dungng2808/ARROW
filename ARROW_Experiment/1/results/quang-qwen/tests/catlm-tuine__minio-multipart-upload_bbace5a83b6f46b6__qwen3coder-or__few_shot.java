package me.tuine.minio;

import org.junit.Test;
import static org.junit.Assert.*;

public class MinioApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Since the main method just calls SpringApplication.run(), 
        // we can't easily test its behavior without more complex integration testing.
        // However, we can verify that the method exists and is callable.
        assertNotNull(MinioApplication.class.getMethod("main", String[].class));
    }
}
