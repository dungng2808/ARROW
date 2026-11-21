package com.tbp.av.controller;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class HelloController_ArrowTest {

    @Test
    public void testIndex() {
        HelloController controller = new HelloController();
        String result = controller.index();
        assertEquals("It is working!", result);
    }
}
