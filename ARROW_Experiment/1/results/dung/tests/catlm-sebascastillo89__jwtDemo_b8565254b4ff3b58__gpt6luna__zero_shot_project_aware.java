package es.softtek.jwtDemo;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class JwtDemoApplication_ArrowTest {

    @Test
    public void applicationCanBeConstructed() {
        JwtDemoApplication application = new JwtDemoApplication();
        assertNotNull(application);
    }
}
