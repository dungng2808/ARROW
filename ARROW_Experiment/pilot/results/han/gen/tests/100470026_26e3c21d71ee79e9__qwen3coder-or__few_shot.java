package javaeetutorial.standalone.ejb;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class StandaloneBean_ArrowTest {

    @Test
    public void testReturnMessage() {
        StandaloneBean standaloneBean = new StandaloneBean();
        String expectedMessage = "Greetings!";
        String actualMessage = standaloneBean.returnMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}
