package com.whatsapp.api.domain.templates;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuickReplyButton_ArrowTest {

    @Test
    public void constructorSetsQuickReplyTypeAndText() {
        QuickReplyButton button = new QuickReplyButton("Reply");

        assertEquals(ButtonType.QUICK_REPLY, button.getType());
        assertEquals("Reply", button.getText());
    }
}
