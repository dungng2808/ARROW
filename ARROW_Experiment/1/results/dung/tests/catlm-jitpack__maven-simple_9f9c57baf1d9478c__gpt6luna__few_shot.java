package com.github.jitpack;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class App_ArrowTest {

    @Test
    public void greetReturnsGreetingForName() {
        App app = new App();
        assertEquals("Hello Alice", app.greet("Alice"));
    }

    @Test
    public void greetReturnsGreetingForEmptyName() {
        App app = new App();
        assertEquals("Hello ", app.greet(""));
    }

    @Test
    public void greetReturnsGreetingForNullName() {
        App app = new App();
        assertEquals("Hello null", app.greet(null));
    }
}
