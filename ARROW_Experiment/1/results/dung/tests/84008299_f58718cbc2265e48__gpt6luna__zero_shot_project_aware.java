package com.tbp.av.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HelloController_ArrowTest {

    @Test
    public void indexReturnsWorkingMessage() {
        HelloController controller = new HelloController();

        assertEquals("It is working!", controller.index());
    }
}
