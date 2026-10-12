package com.example.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Fails in every run: must end up in baseline.failing_in_all_runs. */
class BrokenTests {

    @Test
    void alwaysFails() {
        assertEquals(1, 2);
    }
}
