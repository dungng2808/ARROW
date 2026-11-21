package com.stripe.interview;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Main_ArrowTest {

    @Test
    public void escapesHtmlCharacters() {
        String result = Main.useGuavaForSomeReason("<div title=\"a&b\">'hello'</div>");
        assertEquals("&lt;div title=&quot;a&amp;b&quot;&gt;&#39;hello&#39;&lt;/div&gt;", result);
    }

    @Test
    public void leavesPlainTextUnchanged() {
        String result = Main.useGuavaForSomeReason("plain text");
        assertEquals("plain text", result);
    }

    @Test
    public void escapesEmptyString() {
        String result = Main.useGuavaForSomeReason("");
        assertEquals("", result);
    }
}
