package com.mycompany.app;

import junit.framework.TestCase;
import static org.junit.Assert.assertEquals;

public class App_ArrowTest extends TestCase {

    public void testConstructor() {
        App app = new App();
        assertNotNull(app);
    }

    public void testGetMessage() {
        App app = new App();
        assertEquals("Hello World!", app.getMessage());
    }
}
