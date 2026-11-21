package com.github.jitpack;

import org.junit.Test;
import static org.junit.Assert.*;

public class App_ArrowTest {

    @Test
    public void testGreet() {
        App app = new App();
        String result = app.greet("world");
        assertEquals("Hello world", result);
    }

    @Test
    public void testGreetWithDifferentName() {
        App app = new App();
        String result = app.greet("JUnit");
        assertEquals("Hello JUnit", result);
    }

    @Test
    public void testGreetWithEmptyString() {
        App app = new App();
        String result = app.greet("");
        assertEquals("Hello ", result);
    }
}
