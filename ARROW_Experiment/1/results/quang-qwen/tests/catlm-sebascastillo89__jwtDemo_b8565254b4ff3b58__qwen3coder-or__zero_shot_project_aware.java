package es.softtek.jwtDemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JwtDemoApplication_ArrowTest {

    @Test
    public void testMain() {
        // Test that main method can be called without exception
        // This test verifies the application can start
        try {
            JwtDemoApplication.main(new String[]{});
            // If we reach here, main executed successfully
            assertThat(true).isTrue();
        } catch (Exception e) {
            // This should not happen in normal circumstances
            assertThat(e).isNull();
        }
    }

    @Test
    public void testWebSecurityConfigExists() {
        // Test that the WebSecurityConfig inner class exists and can be instantiated
        JwtDemoApplication app = new JwtDemoApplication();
        JwtDemoApplication.WebSecurityConfig webSecurityConfig = app.new WebSecurityConfig();
        assertThat(webSecurityConfig).isNotNull();
    }
}
