package com.example.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TextUtilsTest {

    @Test
    void capitalizes() {
        assertEquals("Abc", TextUtils.capitalize("abc"));
    }
}
