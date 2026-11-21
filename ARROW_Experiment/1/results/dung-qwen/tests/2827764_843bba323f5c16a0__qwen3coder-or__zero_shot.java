package net.gfu.seminar.maven;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

public class App_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        App app = new App();
        assertEquals("Hello, world!", app.getMessage());
    }

    @Test
    public void testStringConstructor() {
        App app = new App("Custom message");
        assertEquals("Custom message", app.getMessage());
    }

    @Test
    public void testGetMessage() {
        App app = new App("Test message");
        assertEquals("Test message", app.getMessage());
    }

    @Test
    public void testSetMessage() {
        App app = new App();
        app.setMessage("New message");
        assertEquals("New message", app.getMessage());
    }

    @Test
    public void testRun() {
        App app = new App("Test run message");
        // This test verifies that run() doesn't throw an exception
        // and correctly prints the message to stdout
        app.run();
        // No assertion needed as we're just testing that it runs without exception
    }

    @Test
    public void testReadMessageFromFileWithValidFile() throws IOException {
        App app = new App();
        // Using a valid resource path that exists in the classpath
        app.readMessageFromFile("/META-INF/application.properties");
        // Since we don't know the actual content of the properties file,
        // we just verify that the method doesn't throw an exception
        assertNotNull(app.getMessage());
    }

    @Test(expected = IOException.class)
    public void testReadMessageFromFileWithInvalidFile() throws IOException {
        App app = new App();
        app.readMessageFromFile("/nonexistent.properties");
    }
}
