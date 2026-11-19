package org.se.lab;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.sql.Connection;

import static org.junit.Assert.*;

public class Registry_ArrowTest {

    private static final String TEST_PROPERTY_FILE = "test-jdbc.properties";

    @Before
    public void setUp() throws IOException {
        // Create a temporary property file for testing
        PrintWriter writer = new PrintWriter(TEST_PROPERTY_FILE);
        writer.println("jdbc.driver=org.h2.Driver");
        writer.println("jdbc.url=jdbc:h2:mem:testdb");
        writer.println("jdbc.username=testuser");
        writer.println("jdbc.password=testpass");
        writer.close();
        
        // Temporarily modify the PROPERTY_FILE_NAME constant to point to our test file
        try {
            java.lang.reflect.Field field = Registry.class.getDeclaredField("PROPERTY_FILE_NAME");
            field.setAccessible(true);
            field.set(null, TEST_PROPERTY_FILE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetInstance() {
        Registry registry1 = Registry.getInstance();
        Registry registry2 = Registry.getInstance();
        assertSame(registry1, registry2);
    }

    @Test
    public void testGetJdbcDriver() {
        Registry registry = Registry.getInstance();
        assertEquals("org.h2.Driver", registry.getJdbcDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        Registry registry = Registry.getInstance();
        assertEquals("jdbc:h2:mem:testdb", registry.getJdbcUrl());
    }

    @Test
    public void testGetJdbcUsername() {
        Registry registry = Registry.getInstance();
        assertEquals("testuser", registry.getJdbcUsername());
    }

    @Test
    public void testGetJdbcPassword() {
        Registry registry = Registry.getInstance();
        assertEquals("testpass", registry.getJdbcPassword());
    }

    @Test
    public void testGetJdbcConnection() {
        Registry registry = Registry.getInstance();
        assertNull(registry.getJdbcConnection());
    }

    @Test(expected = IllegalStateException.class)
    public void testConstructorWithMissingPropertyFile() {
        try {
            // Temporarily modify the PROPERTY_FILE_NAME constant to point to a non-existent file
            java.lang.reflect.Field field = Registry.class.getDeclaredField("PROPERTY_FILE_NAME");
            field.setAccessible(true);
            field.set(null, "nonexistent.properties");
            
            // This should throw an IllegalStateException
            Registry registry = Registry.getInstance();
        } finally {
            // Restore the original property file name
            try {
                field.set(null, "test-jdbc.properties");
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Test
    public void testSingletonInstanceIsTheSame() {
        Registry registry = Registry.getInstance();
        assertNotNull(registry);
        assertSame(Registry.getInstance(), registry);
    }
}
