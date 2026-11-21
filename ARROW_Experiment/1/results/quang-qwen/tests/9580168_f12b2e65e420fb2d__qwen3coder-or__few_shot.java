package com.qulice.plugin.alpha;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Main_ArrowTest {

    @Test
    public void shouldCalculateSquareOfNumber() {
        int input = 5;
        int expected = 25;
        int actual = Main.square(input);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldCalculateSquareOfZero() {
        int input = 0;
        int expected = 0;
        int actual = Main.square(input);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldCalculateSquareOfNegativeNumber() {
        int input = -3;
        int expected = 9;
        int actual = Main.square(input);
        assertEquals(expected, actual);
    }
}
