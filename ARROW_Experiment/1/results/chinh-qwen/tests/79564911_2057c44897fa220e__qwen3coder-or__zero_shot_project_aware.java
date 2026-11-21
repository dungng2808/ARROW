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
    public void testGetReturnsSameType() {
        RevealService service = RevealServiceFactory.get();
        assertNotNull(service);
        assertSame(RevealService.class, service.getClass());
    }
}
