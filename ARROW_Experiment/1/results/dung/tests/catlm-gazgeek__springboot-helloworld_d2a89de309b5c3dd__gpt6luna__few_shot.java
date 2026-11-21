package com.gazgeek.helloworld.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HomeController_ArrowTest {

    @Test
    public void homeReturnsGreeting() {
        HomeController controller = new HomeController();
        assertEquals("Hello from GazGeek!", controller.home());
    }
}
