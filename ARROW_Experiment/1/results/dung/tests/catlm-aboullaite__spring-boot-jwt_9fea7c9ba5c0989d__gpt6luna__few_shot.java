package me.aboullaite;

import org.junit.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpringBootJwtApplication_ArrowTest {

    @Test
    public void jwtFilterRegistersSecureUrlPattern() {
        SpringBootJwtApplication application = new SpringBootJwtApplication();

        FilterRegistrationBean registrationBean = application.jwtFilter();

        assertNotNull(registrationBean.getFilter());
        assertEquals(java.util.Collections.singletonList("/secure/*"), registrationBean.getUrlPatterns());
    }
}
