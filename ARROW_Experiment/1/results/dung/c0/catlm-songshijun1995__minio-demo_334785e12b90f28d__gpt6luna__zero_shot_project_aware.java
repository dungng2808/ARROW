package com.minio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MinioDemoApplication_ArrowTest {

    @Test
    public void applicationCanBeConstructed() {
        MinioDemoApplication application = new MinioDemoApplication();

        assertNotNull(application);
    }

    @Test
    public void mainStartsApplication() {
        SpringApplication application = SpringApplication.run(
                MinioDemoApplication.class,
                new String[]{"--spring.main.web-application-type=none"});

        assertNotNull(application);
        application.close();
    }
}
