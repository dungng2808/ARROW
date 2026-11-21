package com.gazgeek.helloworld.controller;

import org.junit.Test;
import static org.junit.Assert.*;

public class HomeController_ArrowTest {

    @Test
    public void testHome() {
        HomeController controller = new HomeController();
        String result = controller.home();
        assertEquals("Hello from GazGeek!", result);
    }
}
