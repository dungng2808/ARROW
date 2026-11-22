package javaeetutorial.standalone.ejb;

import org.junit.Test;
import static org.junit.Assert.*;

public class StandaloneBean_ArrowTest {

    @Test
    public void testReturnMessage() {
        StandaloneBean standaloneBean = new StandaloneBean();
        String result = standaloneBean.returnMessage();
        assertEquals("Greetings!", result);
    }
}
