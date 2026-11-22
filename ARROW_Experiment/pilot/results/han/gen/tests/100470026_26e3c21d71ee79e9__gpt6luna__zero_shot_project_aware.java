package javaeetutorial.standalone.ejb;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StandaloneBean_ArrowTest {

    @Test
    public void returnMessageReturnsGreeting() {
        StandaloneBean bean = new StandaloneBean();

        assertEquals("Greetings!", bean.returnMessage());
    }
}
