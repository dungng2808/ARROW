package com.example.calc;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

/** Fails on the first run of a workspace and passes afterwards: must end up in baseline.flaky. */
public class FlakyTest {

    @Test
    public void failsFirstTime() throws IOException {
        File marker = new File("target/flaky.marker");
        boolean seen = marker.exists();
        marker.getParentFile().mkdirs();
        marker.createNewFile();
        assertTrue("first run of this workspace", seen);
    }
}
