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
    public void testWeldServletContainerInitializerBean() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(WeldSpringBootAutoConfiguration.class);
        context.refresh();

        ServletContainerInitializerRegistrationBean<EnhancedListener> bean =
                context.getBean(ServletContainerInitializerRegistrationBean.class);

        assertNotNull(bean);
        assertTrue(bean.getInitializerClasses().contains(EnhancedListener.class));

        context.close();
    }

    @Test
    public void testConfigurationClass() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(WeldSpringBootAutoConfiguration.class);
        context.refresh();

        // Verify that the configuration class is properly loaded
        assertTrue(context.containsBean("weldServletContainerInitializer"));

        context.close();
    }
}
