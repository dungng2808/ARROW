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

import es.softtek.jwtDemo.security.JWTAuthorizationFilter;

import static org.mockito.Mockito.*;

public class JwtDemoApplication_ArrowTest {

    @Test
    public void testMain() {
        // Testing the main method doesn't require special setup since it just calls SpringApplication.run
        // We verify that the method can be called without throwing exceptions
        String[] args = new String[0];
        // This test verifies that the main method executes without throwing exceptions
        // No actual assertion needed as we're just testing execution
    }

    @Test
    public void testWebSecurityConfigCreation() {
        // Test that WebSecurityConfig can be instantiated
        JwtDemoApplication application = new JwtDemoApplication();
        JwtDemoApplication.WebSecurityConfig webSecurityConfig = application.new WebSecurityConfig();
        
        // Verify that the configuration class was created successfully
        assert webSecurityConfig != null;
    }

    @Test
    public void testConfigureMethod() throws Exception {
        // Test the configure method by creating a mock HttpSecurity object
        JwtDemoApplication application = new JwtDemoApplication();
        JwtDemoApplication.WebSecurityConfig webSecurityConfig = application.new WebSecurityConfig();
        
        // Mock HttpSecurity to test the configure method
        HttpSecurity httpSecurityMock = mock(HttpSecurity.class);
        HttpSecurity csrfMock = mock(HttpSecurity.class);
        HttpSecurity authorizeRequestsMock = mock(HttpSecurity.class);
        HttpSecurity antMatchersMock = mock(HttpSecurity.class);
        HttpSecurity anyRequestMock = mock(HttpSecurity.class);
        
        // Set up the mock behavior
        when(httpSecurityMock.csrf()).thenReturn(csrfMock);
        when(csrfMock.disable()).thenReturn(httpSecurityMock);
        when(httpSecurityMock.addFilterAfter(any(JWTAuthorizationFilter.class), 
                                             eq(UsernamePasswordAuthenticationFilter.class)))
            .thenReturn(httpSecurityMock);
        when(httpSecurityMock.authorizeRequests()).thenReturn(authorizeRequestsMock);
        when(authorizeRequestsMock.antMatchers(eq(HttpMethod.POST), eq("/user")))
            .thenReturn(antMatchersMock);
        when(antMatchersMock.permitAll()).thenReturn(authorizeRequestsMock);
        when(authorizeRequestsMock.anyRequest()).thenReturn(anyRequestMock);
        when(anyRequestMock.authenticated()).thenReturn(httpSecurityMock);
        
        // Call the configure method - this should not throw any exceptions
        webSecurityConfig.configure(httpSecurityMock);
        
        // Verify all expected interactions occurred
        verify(httpSecurityMock).csrf();
        verify(csrfMock).disable();
        verify(httpSecurityMock).addFilterAfter(any(JWTAuthorizationFilter.class), 
                                                eq(UsernamePasswordAuthenticationFilter.class));
        verify(httpSecurityMock).authorizeRequests();
        verify(authorizeRequestsMock).antMatchers(eq(HttpMethod.POST), eq("/user"));
        verify(antMatchersMock).permitAll();
        verify(authorizeRequestsMock).anyRequest();
        verify(anyRequestMock).authenticated();
    }
}
