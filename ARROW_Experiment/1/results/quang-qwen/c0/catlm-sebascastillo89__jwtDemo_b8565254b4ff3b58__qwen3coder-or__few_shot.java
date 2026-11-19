package es.softtek.jwtDemo;

import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JwtDemoApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // This test verifies that the main method can be called without throwing exceptions
        // We cannot fully test the Spring application startup in isolation
        // but we can at least verify the method exists and is callable
        JwtDemoApplication.main(new String[]{});
    }

    @Test
    public void testWebSecurityConfigCreation() {
        // Test that the inner class WebSecurityConfig can be instantiated
        JwtDemoApplication application = new JwtDemoApplication();
        JwtDemoApplication.WebSecurityConfig webSecurityConfig = application.new WebSecurityConfig();
        
        // Verify that the class structure is correct
        assert webSecurityConfig != null;
    }

    @Test
    public void testWebSecurityConfigConfigureMethod() throws Exception {
        // Test that the configure method can be called
        // We mock the HttpSecurity to avoid actual security configuration
        JwtDemoApplication application = new JwtDemoApplication();
        JwtDemoApplication.WebSecurityConfig webSecurityConfig = application.new WebSecurityConfig();
        
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        HttpSecurity chainedHttpSecurity1 = mock(HttpSecurity.class);
        HttpSecurity chainedHttpSecurity2 = mock(HttpSecurity.class);
        HttpSecurity chainedHttpSecurity3 = mock(HttpSecurity.class);
        
        // Mock the chaining behavior
        when(httpSecurity.csrf()).thenReturn(chainedHttpSecurity1);
        when(chainedHttpSecurity1.disable()).thenReturn(chainedHttpSecurity2);
        when(chainedHttpSecurity2.addFilterAfter(any(UsernamePasswordAuthenticationFilter.class), any(Class.class)))
                .thenReturn(chainedHttpSecurity3);
        when(chainedHttpSecurity3.authorizeRequests()).thenReturn(chainedHttpSecurity3);
        when(chainedHttpSecurity3.antMatchers(HttpMethod.POST, "/user")).thenReturn(chainedHttpSecurity3);
        when(chainedHttpSecurity3.permitAll()).thenReturn(chainedHttpSecurity3);
        when(chainedHttpSecurity3.anyRequest()).thenReturn(chainedHttpSecurity3);
        when(chainedHttpSecurity3.authenticated()).thenReturn(chainedHttpSecurity3);
        
        // This should not throw an exception
        webSecurityConfig.configure(httpSecurity);
    }

    // Helper method to avoid compilation issues with generics
    private <T> T any(Class<T> clazz) {
        return null;
    }
}
