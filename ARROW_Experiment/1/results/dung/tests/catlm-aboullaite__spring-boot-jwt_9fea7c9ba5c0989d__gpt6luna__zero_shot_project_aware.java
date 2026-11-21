package me.aboullaite;

import org.junit.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpringBootJwtApplication_ArrowTest {

    @Test
    public void jwtFilterRegistersFilterAndSecureUrlPattern() {
        SpringBootJwtApplication application = new SpringBootJwtApplication();

        FilterRegistrationBean registrationBean = application.jwtFilter();

        assertNotNull(registrationBean.getFilter());
        assertEquals(1, registrationBean.getUrlPatterns().size());
        assertEquals("/secure/*", registrationBean.getUrlPatterns().iterator().next());
    }
}
