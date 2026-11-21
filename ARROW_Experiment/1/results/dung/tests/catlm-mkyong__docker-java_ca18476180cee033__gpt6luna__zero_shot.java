package com.mkyong;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class App_ArrowTest {

    @Test
    public void mainWithNoArgumentsPrintsUsage() throws Exception {
        java.io.PrintStream originalErr = System.err;
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        try {
            System.setErr(new java.io.PrintStream(output));
            App.main(new String[0]);
            assertEquals("[Usage] jar -jar find-links.jar <url>" + System.lineSeparator(),
                    output.toString());
        } finally {
            System.setErr(originalErr);
        }
    }
}
