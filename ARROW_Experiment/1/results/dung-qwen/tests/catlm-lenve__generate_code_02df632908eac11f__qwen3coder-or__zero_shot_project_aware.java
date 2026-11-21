package org.javaboy.generate_code;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class GenerateCodeApplication_ArrowTest {

    @Test
    public void testMain() {
        // Since the main method just calls SpringApplication.run(), 
        // we can verify that it was called with correct arguments
        String[] args = new String[]{"test", "args"};
        
        // Mock SpringApplication to verify the call
        try (var mockedStatic = mockStatic(SpringApplication.class)) {
            GenerateCodeApplication.main(args);
            
            // Verify that SpringApplication.run was called with correct parameters
            mockedStatic.verify(() -> SpringApplication.run(
                eq(GenerateCodeApplication.class), 
                eq(args)
            ));
        }
    }
}
