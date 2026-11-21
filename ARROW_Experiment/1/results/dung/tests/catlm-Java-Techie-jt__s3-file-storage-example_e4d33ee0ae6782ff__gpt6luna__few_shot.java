package com.javatechie.s3;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class FileStorageServiceApplication_ArrowTest {

    @Test
    public void canCreateApplication() {
        FileStorageServiceApplication application = new FileStorageServiceApplication();
        assertNotNull(application);
    }
}
