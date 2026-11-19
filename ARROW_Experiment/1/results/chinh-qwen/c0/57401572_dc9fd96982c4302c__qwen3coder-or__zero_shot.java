package org.joinfaces.autoconfigure.weld;

import org.jboss.weld.environment.servlet.EnhancedListener;
import org.joinfaces.autoconfigure.servlet.initializer.ServletContainerInitializerRegistrationBean;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WeldSpringBootAutoConfiguration_ArrowTest {

    @Test
    public void testWeldServletContainerInitializer() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(WeldSpringBootAutoConfiguration.class);
        context.refresh();

        ServletContainerInitializerRegistrationBean<EnhancedListener> result =
                context.getBean(ServletContainerInitializerRegistrationBean.class);

        assertNotNull(result);
        assertTrue(result.getInitializerClasses().contains(EnhancedListener.class));
    }

    @Configuration
    static class TestConfig {
        @Bean
        public ServletContainerInitializerRegistrationBean<EnhancedListener> weldServletContainerInitializer() {
            return new ServletContainerInitializerRegistrationBean<>(EnhancedListener.class);
        }
    }
}
