package com.whitepaek.demosigninwithapple;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class Application_ArrowTest {

    @Test
    public void mainRunsWithoutThrowingForEmptyArguments() {
        assertDoesNotThrow(() -> Application.main(new String[0]));
    }
}
