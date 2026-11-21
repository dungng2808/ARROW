package com.whatsapp.api.domain.templates;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuickReplyButton_ArrowTest {

    @Test
    public void constructorSetsText() {
        QuickReplyButton button = new QuickReplyButton("Reply");
        assertEquals("Reply", button.getText());
    }
}
