package org.javaboy.generate_code;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class GenerateCodeApplication_ArrowTest {

    @Test
    public void canCreateApplication() {
        GenerateCodeApplication application = new GenerateCodeApplication();
        assertNotNull(application);
    }
}
