package com.example.calc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CalculatorTest {

    @Test
    public void addsNumbers() {
        assertEquals(5, new Calculator("c").add(2, 3));
    }

    @Test
    public void dividesNumbers() {
        assertEquals(2, new Calculator("c").divide(6, 3));
    }
}
