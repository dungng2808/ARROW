package com.minio;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class MinioDemoApplication_ArrowTest {

    @Test
    public void testMainMethod() {
        // Test that the main method can be called without throwing an exception
        // This test verifies the basic functionality of the main method
        assertThatCode(() -> MinioDemoApplication.main(new String[]{}))
                .doesNotThrowAnyException();
    }
}
