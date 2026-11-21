package me.aboullaite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import me.aboullaite.config.JwtFilter;
import org.junit.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

public class SpringBootJwtApplication_ArrowTest {

    @Test
    public void jwtFilterRegistersFilterAndSecureUrlPattern() {
        SpringBootJwtApplication application = new SpringBootJwtApplication();

        FilterRegistrationBean registrationBean = application.jwtFilter();

        assertNotNull(registrationBean);
        assertNotNull(registrationBean.getFilter());
        assertEquals(JwtFilter.class, registrationBean.getFilter().getClass());
        Collection<String> urlPatterns = registrationBean.getUrlPatterns();
        assertEquals(1, urlPatterns.size());
        assertEquals("/secure/*", urlPatterns.iterator().next());
    }
}
