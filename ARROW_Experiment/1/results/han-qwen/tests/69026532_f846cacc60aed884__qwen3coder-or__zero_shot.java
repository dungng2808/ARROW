package org.se.lab;

import org.junit.Test;
import static org.junit.Assert.*;

public class Registry_ArrowTest {

    @Test
    public void testGetInstanceReturnsSameInstance() {
        Registry registry1 = Registry.getInstance();
        Registry registry2 = Registry.getInstance();
        assertSame(registry1, registry2);
    }

    @Test
    public void testGetJdbcDriver() {
        Registry registry = Registry.getInstance();
        assertNotNull(registry.getJdbcDriver());
    }

    @Test
    public void testGetJdbcUrl() {
        Registry registry = Registry.getInstance();
        assertNotNull(registry.getJdbcUrl());
    }

    @Test
    public void testGetJdbcUsername() {
        Registry registry = Registry.getInstance();
        assertNotNull(registry.getJdbcUsername());
    }

    @Test
    public void testGetJdbcPassword() {
        Registry registry = Registry.getInstance();
        assertNotNull(registry.getJdbcPassword());
    }

    @Test
    public void testGetJdbcConnectionIsNull() {
        Registry registry = Registry.getInstance();
        assertNull(registry.getJdbcConnection());
    }
}
