package com.whatsapp.api.domain.templates;

import com.whatsapp.api.domain.templates.type.ButtonType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QuickReplyButton_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        QuickReplyButton button = new QuickReplyButton();
        assertEquals(ButtonType.QUICK_REPLY, button.getType());
    }

    @Test
    public void testTextConstructor() {
        String text = "Test Text";
        QuickReplyButton button = new QuickReplyButton(text);
        assertEquals(ButtonType.QUICK_REPLY, button.getType());
        assertEquals(text, button.getText());
    }
}
