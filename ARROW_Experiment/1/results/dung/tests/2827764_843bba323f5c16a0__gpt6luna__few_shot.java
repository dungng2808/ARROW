package net.gfu.seminar.maven;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class App_ArrowTest {

    @Test
    public void defaultConstructorSetsDefaultMessage() {
        App app = new App();
        assertEquals("Hello, world!", app.getMessage());
    }

    @Test
    public void constructorSetsProvidedMessage() {
        App app = new App("Welcome");
        assertEquals("Welcome", app.getMessage());
    }

    @Test
    public void setMessageUpdatesMessage() {
        App app = new App();
        app.setMessage("Updated");
        assertEquals("Updated", app.getMessage());
    }

    @Test
    public void setMessageAcceptsNull() {
        App app = new App();
        app.setMessage(null);
        assertNull(app.getMessage());
    }

    @Test
    public void runPrintsMessage() {
        App app = new App("Hello from app");
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        java.io.PrintStream original = System.out;
        try {
            System.setOut(new java.io.PrintStream(output));
            app.run();
        } finally {
            System.setOut(original);
        }
        assertEquals("Hello from app" + System.lineSeparator(), output.toString());
    }
}
