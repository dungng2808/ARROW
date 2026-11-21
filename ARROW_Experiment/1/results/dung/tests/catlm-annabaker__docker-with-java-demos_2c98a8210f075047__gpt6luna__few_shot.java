package com.example.demo;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DemoApplication_ArrowTest {

    @Test
    public void canCreateApplication() {
        DemoApplication application = new DemoApplication();
        assertNotNull(application);
    }
}
