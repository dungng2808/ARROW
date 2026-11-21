package com.mycompany.app;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class App_ArrowTest {

    @Test
    public void testGetMessage() {
        App app = new App();
        assertEquals("Hello World!", app.getMessage());
    }

    @Test
    public void testMainMethod() {
        // This test verifies that main method can be called without exception
        // The actual output goes to System.out which we cannot easily capture here
        App.main(new String[]{});
    }
}
