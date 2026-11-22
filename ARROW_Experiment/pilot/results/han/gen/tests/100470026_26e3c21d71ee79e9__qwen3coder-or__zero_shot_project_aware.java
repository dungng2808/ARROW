package javaeetutorial.standalone.ejb;

import org.junit.Test;
import static org.junit.Assert.*;

public class StandaloneBean_ArrowTest {

    @Test
    public void testReturnMessage() {
        StandaloneBean bean = new StandaloneBean();
        String result = bean.returnMessage();
        assertEquals("Greetings!", result);
    }
}
