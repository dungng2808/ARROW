package me.tuine.minio;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class MinioApplication_ArrowTest {

    @Test
    public void canCreateApplication() {
        MinioApplication application = new MinioApplication();
        assertNotNull(application);
    }
}
