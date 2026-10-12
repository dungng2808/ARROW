package com.example.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Fails in every run: must end up in baseline.failing_in_all_runs. */
class BrokenTest {

    @Test
    void alwaysFails() {
        assertEquals(1, 2);
    }
}
