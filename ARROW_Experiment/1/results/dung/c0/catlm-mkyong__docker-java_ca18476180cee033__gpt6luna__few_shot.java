package com.mkyong;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class App_ArrowTest {

    @Test
    public void mainWithoutArgumentsPrintsUsage() throws Exception {
        PrintStream originalError = System.err;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(output));
            App.main(new String[0]);
        } finally {
            System.setErr(originalError);
        }

        assertEquals("[Usage] jar -jar find-links.jar <url>" + System.lineSeparator(),
                output.toString());
    }
}
