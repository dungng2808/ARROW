package com.facebook.nailgun;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NGSessionPool_ArrowTest {

    @Test
    public void testConstructorWithNullInstanceCreator() {
        NGServer server = Mockito.mock(NGServer.class);
        NGSessionPool pool = new NGSessionPool(server, 5);
        
        assertNotNull(pool.idlePool);
        assertNotNull(pool.workingPool);
        assertEquals(5, pool.maxIdleSessions);
        assertSame(server, pool.server);
        assertNotNull(pool.instanceCreator);
    }

    @Test
    public void testConstructorWithCustomInstanceCreator() {
        NGServer server = Mockito.mock(NGServer.class);
        Supplier<NGSession> creator = Mockito.mock(Supplier.class);
        NGSessionPool pool = new NGSessionPool(server, 3, creator);
        
        assertSame(creator, pool.instanceCreator);
    }

    @Test
    public void testTakeWhenPoolEmpty() throws Exception {
        NGServer server = Mockito.mock(NGServer.class);
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        NGSession session = pool.take();
        
        assertNotNull(session);
        assertTrue(pool.workingPool.contains(session));
        assertEquals(0, pool.idlePool.size());
        assertEquals(1, pool.workingPool.size());
    }

    @Test
    public void testTakeWhenPoolHasIdleSessions() throws Exception {
        NGServer server = Mockito.mock(NGServer.class);
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        NGSession idleSession = Mockito.mock(NGSession.class);
        pool.idlePool.add(idleSession);
        
        NGSession takenSession = pool.take();
        
        assertSame(idleSession, takenSession);
        assertTrue(pool.workingPool.contains(takenSession));
        assertEquals(0, pool.idlePool.size());
        assertEquals(1, pool.workingPool.size());
    }

    @Test
    public void testGiveWhenPoolNotFull() throws Exception {
        NGServer server = Mockito.mock(NGServer.class);
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        NGSession session = Mockito.mock(NGSession.class);
        pool.workingPool.add(session);
        
        pool.give(session);
        
        assertFalse(pool.workingPool.contains(session));
        assertTrue(pool.idlePool.contains(session));
        assertEquals(1, pool.idlePool.size());
    }

    @Test
    public void testGiveWhenPoolFull() throws Exception {
        NGServer server = Mockito.mock(NGServer.class);
        NGSessionPool pool = new NGSessionPool(server, 1);
        
        NGSession session1 = Mockito.mock(NGSession.class);
        NGSession session2 = Mockito.mock(NGSession.class);
        
        pool.idlePool.add(session1);
        pool.workingPool.add(session2);
        
        pool.give(session2);
        
        assertFalse(pool.workingPool.contains(session2));
        assertFalse(pool.idlePool.contains(session2));
        verify(session2).shutdown();
    }

    @Test
    public void testShutdown() throws Exception {
        NGServer server = Mockito.mock(NGServer.class);
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        NGSession session1 = Mockito.mock(NGSession.class);
        NGSession session2 = Mockito.mock(NGSession.class);
        
        pool.workingPool.add(session1);
        pool.idlePool.add(session2);
        
        pool.shutdown();
        
        assertTrue(pool.done);
        assertTrue(pool.workingPool.isEmpty());
        assertTrue(pool.idlePool.isEmpty());
        verify(session1).shutdown();
        verify(session2).shutdown();
    }

    @Test
    public void testTakeAfterShutdown() {
        NGServer server = Mockito.mock(NGServer.class);
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        pool.shutdown();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            pool.take();
        });
    }

    @Test
    public void testGiveAfterShutdown() throws Exception {
        NGServer server = Mockito.mock(NGServer.class);
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        NGSession session = Mockito.mock(NGSession.class);
        pool.workingPool.add(session);
        
        pool.shutdown();
        
        pool.give(session);
        
        // Should not throw exception and should not modify anything
        assertTrue(pool.workingPool.isEmpty());
    }

    @Test
    public void testMaxIdleSessionsCannotBeNegative() {
        NGServer server = Mockito.mock(NGServer.class);
        NGSessionPool pool = new NGSessionPool(server, -5);
        
        assertEquals(0, pool.maxIdleSessions);
    }

    @Test
    public void testMaxIdleSessionsZero() {
        NGServer server = Mockito.mock(NGServer.class);
        NGSessionPool pool = new NGSessionPool(server, 0);
        
        assertEquals(0, pool.maxIdleSessions);
    }
}
