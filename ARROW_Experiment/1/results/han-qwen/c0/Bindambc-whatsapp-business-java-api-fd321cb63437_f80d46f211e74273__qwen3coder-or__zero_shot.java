package com.whatsapp.api.domain.templates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class QuickReplyButton_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        QuickReplyButton button = new QuickReplyButton();
        assertNotNull(button);
    }

    @Test
    public void testStringConstructor() {
        String text = "Test Text";
        QuickReplyButton button = new QuickReplyButton(text);
        assertNotNull(button);
        assertEquals(text, button.getText());
    }
}
