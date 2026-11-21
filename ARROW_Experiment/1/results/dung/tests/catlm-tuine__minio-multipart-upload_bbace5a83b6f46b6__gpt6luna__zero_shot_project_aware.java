package me.tuine.minio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MinioApplication_ArrowTest {

    @Test
    public void applicationCanBeInstantiated() {
        assertNotNull(new MinioApplication());
    }
}
