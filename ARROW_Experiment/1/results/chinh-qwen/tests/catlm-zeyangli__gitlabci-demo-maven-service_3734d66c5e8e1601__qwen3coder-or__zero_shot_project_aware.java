package com.mycompany.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class App_ArrowTest {

    @Test
    public void testConstructor() {
        App app = new App();
        assertNotNull(app);
    }

    @Test
    public void testGetMessage() {
        App app = new App();
        String result = app.getMessage();
        assertEquals("Hello World!", result);
    }
}
