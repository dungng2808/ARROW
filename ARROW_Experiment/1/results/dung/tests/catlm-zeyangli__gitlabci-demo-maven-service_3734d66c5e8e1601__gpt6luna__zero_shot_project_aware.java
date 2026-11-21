package com.mycompany.app;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class App_ArrowTest {

    @Test
    public void mainPrintsHelloWorld() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output));
            App.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }
        assertEquals("Hello World!" + System.lineSeparator(), output.toString());
    }
}
