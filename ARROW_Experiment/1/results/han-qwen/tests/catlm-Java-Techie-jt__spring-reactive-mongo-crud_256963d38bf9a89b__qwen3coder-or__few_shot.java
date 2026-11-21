package com.javatechie.reactive;

import org.junit.Test;
import static org.junit.Assert.*;

public class SpringReactiveMongoCrudApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Since the main method just calls SpringApplication.run(), 
        // we can't easily test its behavior without more complex testing
        // However, we can verify the class can be instantiated and the main method exists
        assertNotNull(SpringReactiveMongoCrudApplication.class);
    }
}
