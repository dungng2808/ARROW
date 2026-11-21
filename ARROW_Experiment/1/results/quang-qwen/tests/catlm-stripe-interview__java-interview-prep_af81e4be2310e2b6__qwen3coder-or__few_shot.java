package com.stripe.interview;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Main_ArrowTest {

    @Test
    public void testMainMethod() {
        // The main method just prints to console, so we can't easily test its output
        // but we can verify the method exists and is callable
        Main.main();
    }

    @Test
    public void testUseGuavaForSomeReason() {
        String input = "<script>alert('xss')</script>";
        String expected = "&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;";
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testUseGuavaForSomeReasonWithEmptyString() {
        String input = "";
        String expected = "";
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testUseGuavaForSomeReasonWithNull() {
        String input = null;
        String actual = Main.useGuavaForSomeReason(input);
        // HtmlEscaper should handle null gracefully
        assertEquals("", actual);
    }
}
