package hello.domain;

import org.junit.Test;
import static org.junit.Assert.*;

public class Hello_ArrowTest {

    @Test
    public void testConstructor() {
        String expectedMessage = "Hello World";
        Hello hello = new Hello(expectedMessage);
        assertEquals(expectedMessage, hello.getMessage());
    }

    @Test
    public void testGetMessage() {
        String message = "Test Message";
        Hello hello = new Hello(message);
        assertEquals(message, hello.getMessage());
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
