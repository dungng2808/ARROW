package ch.cyberduck.core.local;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class RevealServiceFactory_ArrowTest {

    @Test
    public void testGetReturnsNonNull() {
        RevealService service = RevealServiceFactory.get();
        assertNotNull(service);
    }

    @Test
    public void testGetReturnsSameInstance() {
        RevealService service1 = RevealServiceFactory.get();
        RevealService service2 = RevealServiceFactory.get();
        assertSame(service1.getClass(), service2.getClass());
    }
}
