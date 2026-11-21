package org.joinfaces.autoconfigure.weld;

import org.joinfaces.autoconfigure.servlet.initializer.ServletContainerInitializerRegistrationBean;
import org.jboss.weld.environment.servlet.EnhancedListener;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WeldSpringBootAutoConfiguration_ArrowTest {

	@Test
	public void createsWeldServletContainerInitializer() {
		WeldSpringBootAutoConfiguration configuration = new WeldSpringBootAutoConfiguration();

		ServletContainerInitializerRegistrationBean<EnhancedListener> result =
				configuration.weldServletContainerInitializer();

		assertNotNull(result);
		assertEquals(EnhancedListener.class, result.getServletContainerInitializerClass());
	}
}
