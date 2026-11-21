package com.example.quartz;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SampleQuartzApplication_ArrowTest {

    @Test
    public void applicationCanBeCreated() {
        SampleQuartzApplication application = new SampleQuartzApplication();
        assertNotNull(application);
    }
}
