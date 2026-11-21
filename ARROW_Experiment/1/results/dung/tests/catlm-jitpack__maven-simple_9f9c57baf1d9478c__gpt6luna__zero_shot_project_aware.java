package com.github.jitpack;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class App_ArrowTest {
    @Test
    public void greetReturnsGreetingForName() {
        App app = new App();

        assertEquals("Hello world", app.greet("world"));
    }

    @Test
    public void greetConcatenatesNullName() {
        App app = new App();

        assertEquals("Hello null", app.greet(null));
    }

    @Test
    public void greetConcatenatesEmptyName() {
        App app = new App();

        assertEquals("Hello ", app.greet(""));
    }
}
