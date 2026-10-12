package com.example.core;

public final class TextUtils {

    private TextUtils() {
    }

    public static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
