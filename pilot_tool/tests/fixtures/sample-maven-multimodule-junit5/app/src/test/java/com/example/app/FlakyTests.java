package com.example.app;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/** Fails on the first run of a workspace and passes afterwards: must end up in baseline.flaky. */
class FlakyTests {

    @Test
    void failsFirstTime() throws IOException {
        File marker = new File("target/flaky.marker");
        boolean seen = marker.exists();
        marker.getParentFile().mkdirs();
        marker.createNewFile();
        assertTrue(seen, "first run of this workspace");
    }
}
