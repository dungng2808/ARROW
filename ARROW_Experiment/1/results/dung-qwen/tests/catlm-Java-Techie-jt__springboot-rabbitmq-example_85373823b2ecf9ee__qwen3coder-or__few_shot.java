package com.javatechie.rabbitmq.demo;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class SpringbootRabbitmqExampleApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Since the main method just calls SpringApplication.run(), 
        // we can verify it executes without throwing an exception
        try {
            SpringbootRabbitmqExampleApplication.main(new String[]{});
            assertTrue(true); // If we reach here, the method executed successfully
        } catch (Exception e) {
            // This test is mainly to ensure the main method doesn't crash
            // If it does crash, the test will fail naturally
            assertTrue("Main method should not throw exception", false);
        }
    }
}
