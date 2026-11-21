package me.aboullaite;

import me.aboullaite.config.JwtFilter;
import org.junit.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.Assert.*;

public class SpringBootJwtApplication_ArrowTest {

    @Test
    public void testJwtFilter() {
        SpringBootJwtApplication application = new SpringBootJwtApplication();
        FilterRegistrationBean registrationBean = application.jwtFilter();
        
        assertNotNull(registrationBean);
        assertNotNull(registrationBean.getFilter());
        assertTrue(registrationBean.getFilter() instanceof JwtFilter);
        assertEquals("/secure/*", registrationBean.getUrlPatterns().iterator().next());
    }

    @Test
    public void testMainMethod() {
        // This test just verifies that the main method can be called
        // without throwing an exception
        try {
            SpringBootJwtApplication.main(new String[]{});
        } catch (Exception e) {
            fail("Main method should not throw exception: " + e.getMessage());
        }
    }
}
