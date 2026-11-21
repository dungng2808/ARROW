package com.minio;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class MinioDemoApplication_ArrowTest {

    @Test
    public void applicationCanBeInstantiated() {
        MinioDemoApplication application = new MinioDemoApplication();
        assertNotNull(application);
    }
}
