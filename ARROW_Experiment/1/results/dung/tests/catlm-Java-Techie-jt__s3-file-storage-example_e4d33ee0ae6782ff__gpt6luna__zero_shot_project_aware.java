package com.javatechie.s3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileStorageServiceApplication_ArrowTest {

    @Test
    public void applicationCanBeInstantiated() {
        FileStorageServiceApplication application = new FileStorageServiceApplication();

        assertNotNull(application);
    }
}
