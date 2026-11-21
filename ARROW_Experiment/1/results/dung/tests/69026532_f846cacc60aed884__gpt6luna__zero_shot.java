package org.se.lab;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.sql.Connection;

import org.junit.Test;

public class Registry_ArrowTest
{
	@Test
	public void getInstanceReturnsSameSingleton()
	{
		assertSame(Registry.getInstance(), Registry.getInstance());
	}

	@Test
	public void getJdbcConnectionReturnsNull()
	{
		Registry registry = Registry.getInstance();
		Connection connection = registry.getJdbcConnection();
		assertNull(connection);
	}
}
