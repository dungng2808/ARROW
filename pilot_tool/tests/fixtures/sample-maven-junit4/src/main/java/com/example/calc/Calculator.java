package com.example.calc;

/** Class under test of the fixture. */
public class Calculator {

    private final String name;

    public Calculator(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int add(int a, int b) {
        return a + b;
    }

    public int divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("b must not be zero");
        }
        return a / b;
    }

    /** Throws NullPointerException when label is null. */
    public int labelLength(String label) {
        return label.trim().length();
    }
}
