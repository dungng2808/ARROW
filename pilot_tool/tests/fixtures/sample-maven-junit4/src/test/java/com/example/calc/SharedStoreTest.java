package com.example.calc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Test;

/** Passes unless another test wrote to the shared store (regression fixture). */
public class SharedStoreTest {

    @Test
    public void storeIsUntouched() throws IOException {
        assertEquals("ok", SharedStore.read());
    }
}
