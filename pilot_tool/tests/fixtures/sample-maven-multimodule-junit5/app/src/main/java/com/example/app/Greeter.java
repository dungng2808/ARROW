package com.example.app;

import com.example.core.TextUtils;

/** Class under test of the fixture. */
public class Greeter {

    private final String greeting;

    public Greeter(String greeting) {
        this.greeting = greeting;
    }

    public String greet(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name");
        }
        return greeting + ", " + TextUtils.capitalize(name) + "!";
    }

    public int count(String[] names) {
        return names.length;
    }
}
