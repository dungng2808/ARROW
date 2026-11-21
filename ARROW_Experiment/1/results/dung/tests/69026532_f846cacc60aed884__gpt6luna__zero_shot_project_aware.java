package org.se.lab;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.sql.Connection;

import org.junit.Test;

public class Registry_ArrowTest
{
	@Test
	public void getInstanceReturnsSingleton()
	{
		assertSame(Registry.getInstance(), Registry.getInstance());
	}

	@Test
	public void getJdbcConnectionReturnsNull()
	{
		Connection connection = Registry.getInstance().getJdbcConnection();
		assertNull(connection);
	}
}
