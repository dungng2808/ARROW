package com.mkyong;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class App_ArrowTest {

    @Test
    public void mainWithNoArgumentsReturnsNormally() {
        assertDoesNotThrow(() -> App.main(new String[0]));
    }
}
