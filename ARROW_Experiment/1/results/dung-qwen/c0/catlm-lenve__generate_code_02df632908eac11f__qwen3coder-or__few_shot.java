package org.javaboy.generate_code;

import org.junit.Test;
import static org.junit.Assert.*;

public class GenerateCodeApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Since the main method just calls SpringApplication.run(), 
        // we can't easily test its behavior without more complex setup.
        // However, we can verify that the method exists and is callable.
        assertNotNull(GenerateCodeApplication.class.getMethod("main", String[].class));
    }
}
