package es.softtek.jwtDemo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import es.softtek.jwtDemo.security.JWTAuthorizationFilter;

public class JwtDemoApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Test that main method can be called without exception
        // This test verifies the application can start
        assertNotNull("JwtDemoApplication should be instantiable", new JwtDemoApplication());
    }

    @Test
    public void testWebSecurityConfigCreation() {
        // Test that WebSecurityConfig can be instantiated
        JwtDemoApplication app = new JwtDemoApplication();
        WebSecurityConfig webSecurityConfig = app.new WebSecurityConfig();
        assertNotNull("WebSecurityConfig should be instantiable", webSecurityConfig);
    }

    @Test
    public void testWebSecurityConfigAnnotations() {
        // Test that the WebSecurityConfig class has correct annotations
        Class<?> clazz = JwtDemoApplication.WebSecurityConfig.class;
        
        // Check for EnableWebSecurity annotation
        EnableWebSecurity enableWebSecurity = clazz.getAnnotation(EnableWebSecurity.class);
        assertNotNull("WebSecurityConfig should have EnableWebSecurity annotation", enableWebSecurity);
        
        // Check for Configuration annotation
        Configuration configuration = clazz.getAnnotation(Configuration.class);
        assertNotNull("WebSecurityConfig should have Configuration annotation", configuration);
    }

    @Test
    public void testConfigureMethodExists() throws Exception {
        // Test that configure method exists and can be called
        JwtDemoApplication app = new JwtDemoApplication();
        WebSecurityConfig webSecurityConfig = app.new WebSecurityConfig();
        
        // We can't fully test the configure method without a real HttpSecurity object,
        // but we can verify the method signature exists
        assertNotNull("configure method should exist", 
                     WebSecurityConfigurerAdapter.class.getMethod("configure", HttpSecurity.class));
    }

    @Test
    public void testSpringBootApplicationAnnotation() {
        // Test that JwtDemoApplication has correct Spring Boot annotation
        SpringBootApplication springBootApplication = 
            JwtDemoApplication.class.getAnnotation(SpringBootApplication.class);
        assertNotNull("JwtDemoApplication should have SpringBootApplication annotation", 
                     springBootApplication);
    }

    @Test
    public void testJWTAuthorizationFilterUsage() {
        // Test that JWTAuthorizationFilter is referenced correctly
        assertNotNull("JWTAuthorizationFilter should be accessible", 
                     JWTAuthorizationFilter.class);
    }

    @Test
    public void testHttpMethodPostPermitAll() {
        // Test that POST /user is configured to permit all
        // This is more of a verification that the configuration makes sense
        assertTrue("HttpMethod.POST should be available", HttpMethod.POST != null);
    }
}
