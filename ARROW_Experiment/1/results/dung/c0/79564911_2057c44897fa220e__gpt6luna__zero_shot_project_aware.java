package ch.cyberduck.core.local;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class RevealServiceFactory_ArrowTest {
    @Test
    public void getReturnsRevealService() {
        RevealService service = RevealServiceFactory.get();
        assertNotNull(service);
    }
}
