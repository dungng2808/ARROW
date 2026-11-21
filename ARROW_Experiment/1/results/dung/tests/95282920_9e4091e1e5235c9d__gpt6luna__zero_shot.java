package hello.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Hello_ArrowTest {

    @Test
    public void constructorStoresMessage() {
        Hello hello = new Hello("Hello, world");

        assertEquals("Hello, world", hello.getMessage());
    }

    @Test
    public void constructorStoresNullMessage() {
        Hello hello = new Hello(null);

        assertNull(hello.getMessage());
    }

    @Test
    public void setMessageUpdatesMessage() {
        Hello hello = new Hello("before");

        hello.setMessage("after");

        assertEquals("after", hello.getMessage());
    }

    @Test
    public void setMessageAcceptsNull() {
        Hello hello = new Hello("before");

        hello.setMessage(null);

        assertNull(hello.getMessage());
    }
}
