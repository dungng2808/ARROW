package me.tuine.minio;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class MinioApplication_ArrowTest {

    @Test
    public void testMain() {
        // This test verifies that the main method can be called without throwing exceptions
        // Since SpringApplication.run is a static method that starts the Spring Boot application,
        // we can't easily test it without starting the full application context.
        // However, we can verify that the method exists and can be invoked.
        
        // We'll use Mockito to verify that SpringApplication.run was called with correct arguments
        // but since this is a static method, we need to use PowerMockito or similar for true mocking
        // which is not available in the dependencies. So we'll just test that the method doesn't throw
        // an exception when called with null args (which is valid).
        
        // Note: In a real scenario, you would want to test the actual Spring Boot startup behavior
        // but that requires integration testing rather than unit testing.
        
        // This test simply ensures the main method executes without throwing unexpected exceptions
        // when passed null arguments (which is a valid input)
        MinioApplication.main(null);
    }
}
