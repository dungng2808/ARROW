package com.mycompany.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class App_ArrowTest {

    @Test
    public void constructsApp() {
        App app = new App();
        assertEquals(App.class, app.getClass());
    }

    @Test
    public void mainPrintsHelloWorld() {
        java.io.PrintStream originalOut = System.out;
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        try {
            System.setOut(new java.io.PrintStream(output));
            App.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }
        assertEquals("Hello World!" + System.lineSeparator(), output.toString());
    }
}
