package com.qulice.plugin.alpha;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class Main_ArrowTest {

    @Test
    public void testSquare() {
        assertThat(Main.square(5), is(25));
    }

    @Test
    public void testSquareOfZero() {
        assertThat(Main.square(0), is(0));
    }

    @Test
    public void testSquareOfNegative() {
        assertThat(Main.square(-3), is(9));
    }
}
