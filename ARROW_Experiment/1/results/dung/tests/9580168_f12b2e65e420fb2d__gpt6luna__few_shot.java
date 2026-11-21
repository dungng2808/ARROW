package com.qulice.plugin.alpha;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Main_ArrowTest {

    @Test
    public void squaresPositiveNumber() {
        assertEquals(25, Main.square(5));
    }

    @Test
    public void squaresNegativeNumber() {
        assertEquals(25, Main.square(-5));
    }

    @Test
    public void squaresZero() {
        assertEquals(0, Main.square(0));
    }
}
