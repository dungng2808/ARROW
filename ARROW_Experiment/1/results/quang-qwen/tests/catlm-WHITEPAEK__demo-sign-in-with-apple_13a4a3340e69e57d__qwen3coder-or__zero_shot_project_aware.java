package com.whitepaek.demosigninwithapple;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class Application_ArrowTest {

    @Test
    public void testMainMethod() {
        // Since the main method just calls SpringApplication.run(), 
        // we can't easily test its behavior without more complex integration testing.
        // However, we can verify that the method exists and is callable.
        assertThat(Application.class).isNotNull();
        
        // Verify that the main method exists and is accessible
        try {
            Application.class.getMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            fail("Main method should exist");
        }
    }
}
