package com.stripe.interview;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Main_ArrowTest {
    @Test
    public void escapesHtmlCharacters() {
        assertEquals("&lt;&gt;&amp;&quot;&#39;",
                Main.useGuavaForSomeReason("<>&\"'"));
    }

    @Test
    public void leavesPlainTextUnchanged() {
        assertEquals("hello world",
                Main.useGuavaForSomeReason("hello world"));
    }

    @Test
    public void escapesEmptyStringAsEmptyString() {
        assertEquals("", Main.useGuavaForSomeReason(""));
    }
}
