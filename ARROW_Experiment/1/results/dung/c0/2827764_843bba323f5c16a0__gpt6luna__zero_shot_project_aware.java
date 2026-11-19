package net.gfu.seminar.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

public class App_ArrowTest {

    @Test
    public void defaultConstructorSetsHelloWorldMessage() {
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
    public void readMessageFromFileLoadsMessage() throws IOException {
        App app = new App();
        app.readMessageFromFile("/META-INF/application.properties");

        assertEquals("Hello, world!", app.getMessage());
    }
}
