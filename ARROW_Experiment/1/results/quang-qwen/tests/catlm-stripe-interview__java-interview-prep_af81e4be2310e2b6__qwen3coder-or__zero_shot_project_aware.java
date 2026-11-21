package com.stripe.interview;

import com.google.common.html.HtmlEscapers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Main_ArrowTest {

    @Test
    public void testUseGuavaForSomeReason() {
        // Test normal case
        String input = "Hello & welcome <to> the world";
        String expected = HtmlEscapers.htmlEscaper().escape(input);
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testUseGuavaForSomeReasonWithEmptyString() {
        String input = "";
        String expected = HtmlEscapers.htmlEscaper().escape(input);
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testUseGuavaForSomeReasonWithNull() {
        String input = null;
        String expected = HtmlEscapers.htmlEscaper().escape(input);
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testUseGuavaForSomeReasonWithSpecialCharacters() {
        String input = "<script>alert('xss')</script>";
        String expected = HtmlEscapers.htmlEscaper().escape(input);
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }
}
