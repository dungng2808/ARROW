package org.javaboy.generate_code;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GenerateCodeApplication_ArrowTest {

    @Test
    public void applicationCanBeInstantiated() {
        GenerateCodeApplication application = new GenerateCodeApplication();
        assertNotNull(application);
    }

    @Test
    public void mainStartsApplication() {
        GenerateCodeApplication.main(new String[0]);
    }
}
