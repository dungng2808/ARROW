package com.stripe.interview;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Main_ArrowTest {

    @Test
    public void testUseGuavaForSomeReason_NormalInput() {
        String input = "Hello <world>";
        String expected = "Hello &lt;world&gt;";
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testUseGuavaForSomeReason_EmptyString() {
        String input = "";
        String expected = "";
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testUseGuavaForSomeReason_NullInput() {
        String input = null;
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(input, actual);
    }

    @Test
    public void testUseGuavaForSomeReason_WithAmpersand() {
        String input = "Price: $5 & up";
        String expected = "Price: $5 &amp; up";
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testUseGuavaForSomeReason_WithQuotes() {
        String input = "He said \"Hello\"";
        String expected = "He said &quot;Hello&quot;";
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }
}
