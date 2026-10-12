package com.example.calc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Fails in every run: must end up in baseline.failing_in_all_runs. */
public class BrokenTest {

    @Test
    public void alwaysFails() {
        assertEquals(1, 2);
    }
}
