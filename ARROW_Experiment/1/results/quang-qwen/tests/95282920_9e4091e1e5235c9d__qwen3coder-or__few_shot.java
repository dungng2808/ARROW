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
        String expectedMessage = "Test Message";
        Hello hello = new Hello(expectedMessage);
        assertEquals(expectedMessage, hello.getMessage());
    }

    @Test
    public void testSetMessage() {
        Hello hello = new Hello("Initial Message");
        String newMessage = "New Message";
        hello.setMessage(newMessage);
        assertEquals(newMessage, hello.getMessage());
    }
}
