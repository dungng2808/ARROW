package com.whatsapp.api.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class QuickReplyButton_ArrowTest {

    @Test
    public void constructorSetsQuickReplyTypeAndText() {
        QuickReplyButton button = new QuickReplyButton("Reply");

        assertEquals("Reply", button.getText());
        assertEquals(com.whatsapp.api.domain.templates.type.ButtonType.QUICK_REPLY, button.getType());
    }
}
