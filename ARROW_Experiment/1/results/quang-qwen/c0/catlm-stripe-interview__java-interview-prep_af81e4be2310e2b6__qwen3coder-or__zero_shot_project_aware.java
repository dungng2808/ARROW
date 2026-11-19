package com.stripe.interview;

import com.google.common.html.HtmlEscapers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Main_ArrowTest {

    @Test
    public void testUseGuavaForSomeReason() {
        // Test with normal string
        String input = "Hello & welcome";
        String expected = HtmlEscapers.htmlEscaper().escape(input);
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testUseGuavaForSomeReasonWithSpecialCharacters() {
        // Test with special HTML characters
        String input = "<script>alert('xss')</script>";
        String expected = HtmlEscapers.htmlEscaper().escape(input);
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testUseGuavaForSomeReasonWithEmptyString() {
        // Test with empty string
        String input = "";
        String expected = HtmlEscapers.htmlEscaper().escape(input);
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testUseGuavaForSomeReasonWithNull() {
        // Test with null string
        String input = null;
        String expected = HtmlEscapers.htmlEscaper().escape(input);
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }
}
