package org.joinfaces.autoconfigure.weld;

import org.jboss.weld.environment.servlet.EnhancedListener;
import org.joinfaces.autoconfigure.servlet.initializer.ServletContainerInitializerRegistrationBean;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WeldSpringBootAutoConfiguration_ArrowTest {

	@Test
	public void weldServletContainerInitializerReturnsRegistrationBeanForEnhancedListener() {
		WeldSpringBootAutoConfiguration configuration = new WeldSpringBootAutoConfiguration();

		ServletContainerInitializerRegistrationBean<EnhancedListener> result =
				configuration.weldServletContainerInitializer();

		assertThat(result).isNotNull();
		assertThat(result.getServletContainerInitializerClass()).isEqualTo(EnhancedListener.class);
	}
}
