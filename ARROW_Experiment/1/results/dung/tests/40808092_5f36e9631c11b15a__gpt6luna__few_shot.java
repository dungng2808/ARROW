package com.iluwatar.singleton;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class App_ArrowTest {

    @Test
    public void mainPrintsSingletonInstances() {
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        try {
            System.setOut(new java.io.PrintStream(output));
            App.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String[] lines = output.toString().split("\\r?\\n");
        assertEquals(10, lines.length);
        assertEquals(lines[0].substring("ivoryTower1=".length()),
                lines[1].substring("ivoryTower2=".length()));
        assertEquals(lines[2].substring("threadSafeIvoryTower1=".length()),
                lines[3].substring("threadSafeIvoryTower2=".length()));
        assertEquals(lines[4].substring("enumIvoryTower1=".length()),
                lines[5].substring("enumIvoryTower2=".length()));
        assertEquals(lines[6], lines[7]);
        assertEquals(lines[8], lines[9]);
    }
}
