package me.aboullaite;

import me.aboullaite.config.JwtFilter;
import org.junit.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class SpringBootJwtApplication_ArrowTest {

    @Test
    public void testJwtFilter() {
        SpringBootJwtApplication application = new SpringBootJwtApplication();
        FilterRegistrationBean registrationBean = application.jwtFilter();
        
        assertNotNull(registrationBean);
        assertNotNull(registrationBean.getFilter());
        assertEquals(JwtFilter.class, registrationBean.getFilter().getClass());
        assertEquals("/secure/*", registrationBean.getUrlPatterns().iterator().next());
    }
}
