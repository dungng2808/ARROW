package org.se.lab;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.sql.Connection;

import org.junit.Test;

public class Registry_ArrowTest {

    @Test
    public void getInstanceReturnsSingleton() {
        Registry first = Registry.getInstance();
        Registry second = Registry.getInstance();

        assertNotNull(first);
        assertSame(first, second);
    }

    @Test
    public void getJdbcConnectionReturnsNull() {
        Registry registry = Registry.getInstance();

        Connection connection = registry.getJdbcConnection();

        assertNull(connection);
    }
}
