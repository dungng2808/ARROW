package hello.domain;

import org.junit.Test;
import static org.junit.Assert.*;

public class Hello_ArrowTest {

    @Test
    public void testConstructor() {
        String testMessage = "Hello World";
        Hello hello = new Hello(testMessage);
        assertEquals(testMessage, hello.getMessage());
    }

    @Test
    public void testGetMessage() {
        String testMessage = "Test Message";
        Hello hello = new Hello(testMessage);
        assertEquals(testMessage, hello.getMessage());
    }

    @Test
    public void testSetMessage() {
        String initialMessage = "Initial Message";
        String newMessage = "New Message";
        Hello hello = new Hello(initialMessage);
        hello.setMessage(newMessage);
        assertEquals(newMessage, hello.getMessage());
    }
}
