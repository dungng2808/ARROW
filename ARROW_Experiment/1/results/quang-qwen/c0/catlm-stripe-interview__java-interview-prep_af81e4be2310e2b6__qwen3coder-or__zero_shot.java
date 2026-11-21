package com.stripe.interview;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class Main_ArrowTest {

    @Test
    public void testUseGuavaForSomeReason_NormalString() {
        String input = "Hello World";
        String expected = "Hello World";
        String actual = Main.useGuavaForSomeReason(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testUseGuavaForSomeReason_StringWithSpecialCharacters() {
        String input = "<script>alert('xss')</script>";
        String expected = "&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;";
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
        assertEquals(null, actual);
    }
}
