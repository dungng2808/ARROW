package org.joinfaces.autoconfigure.weld;

import org.jboss.weld.environment.servlet.EnhancedListener;
import org.joinfaces.autoconfigure.servlet.initializer.ServletContainerInitializerRegistrationBean;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class WeldSpringBootAutoConfiguration_ArrowTest {

	@Test
	public void weldServletContainerInitializerReturnsRegistrationBean() {
		WeldSpringBootAutoConfiguration configuration = new WeldSpringBootAutoConfiguration();

		ServletContainerInitializerRegistrationBean<EnhancedListener> result =
				configuration.weldServletContainerInitializer();

		assertNotNull(result);
	}
}
