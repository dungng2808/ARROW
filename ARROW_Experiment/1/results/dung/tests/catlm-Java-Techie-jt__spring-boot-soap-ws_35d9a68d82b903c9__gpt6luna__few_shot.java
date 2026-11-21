package com.javatechie.spring.soap.api;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SpringBootSopaWsApplication_ArrowTest {

    @Test
    public void applicationCanBeCreated() {
        SpringBootSopaWsApplication application = new SpringBootSopaWsApplication();
        assertNotNull(application);
    }
}
