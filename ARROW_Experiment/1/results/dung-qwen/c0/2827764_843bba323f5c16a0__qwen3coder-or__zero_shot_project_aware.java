package net.gfu.seminar.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

public class App_ArrowTest {

	@Test
	public void testAppDefaultConstructor() {
		App app = new App();
		assertEquals("Hello, world!", app.getMessage());
	}

	@Test
	public void testAppStringConstructor() {
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
		// This test just verifies that run() doesn't throw an exception
		// since it only prints to System.out
		App app = new App("Test message");
		app.run(); // Should not throw exception
	}

	@Test
	public void testReadMessageFromFileWithValidFile() throws IOException {
		App app = new App();
		// This will attempt to load from /META-INF/application.properties
		// Since we don't have a real file, we expect an IOException
		try {
			app.readMessageFromFile("/META-INF/application.properties");
		} catch (IOException e) {
			// Expected behavior - file doesn't exist in test environment
		}
		// Message should remain unchanged
		assertEquals("Hello, world!", app.getMessage());
	}

	@Test
	public void testReadMessageFromFileWithNullFile() throws IOException {
		App app = new App();
		try {
			app.readMessageFromFile(null);
		} catch (NullPointerException e) {
			// Expected behavior - null file name
		}
		// Message should remain unchanged
		assertEquals("Hello, world!", app.getMessage());
	}

	@Test
	public void testMainMethod() {
		// This test just verifies that main() doesn't throw an exception
		// It will try to read from /META-INF/application.properties which doesn't exist
		// but should handle the IOException gracefully
		App.main(new String[]{}); // Should not throw exception
	}
}
