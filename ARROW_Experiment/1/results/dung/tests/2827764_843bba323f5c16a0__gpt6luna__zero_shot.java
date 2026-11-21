package net.gfu.seminar.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;

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
    public void constructorAllowsNullMessage() {
        App app = new App(null);

        assertNull(app.getMessage());
    }

    @Test
    public void setMessageReplacesCurrentMessage() {
        App app = new App("Before");

        app.setMessage("After");

        assertEquals("After", app.getMessage());
    }

    @Test
    public void setMessageAllowsNull() {
        App app = new App("Before");

        app.setMessage(null);

        assertNull(app.getMessage());
    }

    @Test
    public void runPrintsMessage() {
        App app = new App("Hello");
        java.io.PrintStream original = System.out;
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        try {
            System.setOut(new java.io.PrintStream(output));
            app.run();
        } finally {
            System.setOut(original);
        }

        assertEquals("Hello" + System.lineSeparator(), output.toString());
    }

    @Test
    public void readMessageFromFileLoadsMessageProperty() throws IOException {
        final byte[] properties = "message=Loaded message\n".getBytes("ISO-8859-1");
        App app = new App() {
            @Override
            public java.io.InputStream getClassResourceAsStream(String file) {
                return new ByteArrayInputStream(properties);
            }
        };

        app.readMessageFromFile("/META-INF/application.properties");

        assertEquals("Loaded message", app.getMessage());
    }
}
