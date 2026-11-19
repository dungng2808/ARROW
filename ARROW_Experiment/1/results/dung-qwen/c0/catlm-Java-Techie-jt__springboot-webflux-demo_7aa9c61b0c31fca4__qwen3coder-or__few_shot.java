package com.javatechie.webflux;

import org.junit.Test;
import static org.junit.Assert.*;

public class SpringbootWebfluxDemoApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Since the main method just calls SpringApplication.run(), 
        // we can verify it executes without throwing an exception
        try {
            SpringbootWebfluxDemoApplication.main(new String[]{});
            // If we reach here, the method executed successfully
            assertTrue(true);
        } catch (Exception e) {
            fail("Main method should not throw exception: " + e.getMessage());
        }
    }
}
