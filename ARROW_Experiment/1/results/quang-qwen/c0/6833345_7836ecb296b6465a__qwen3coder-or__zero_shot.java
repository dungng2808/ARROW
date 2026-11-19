package com.facebook.nailgun;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class NGSessionPool_ArrowTest {

    @Test
    public void testConstructorWithMaxIdleZero() {
        NGServer server = new NGServer();
        NGSessionPool pool = new NGSessionPool(server, 0);
        assertEquals(0, pool.maxIdleSessions);
    }

    @Test
    public void testConstructorWithMaxIdlePositive() {
        NGServer server = new NGServer();
        NGSessionPool pool = new NGSessionPool(server, 5);
        assertEquals(5, pool.maxIdleSessions);
    }

    @Test
    public void testTakeFromEmptyPoolCreatesNewSession() throws Exception {
        NGServer server = new NGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        NGSession session = pool.take();
        assertNotNull(session);
        assertTrue(pool.workingPool.contains(session));
        assertEquals(1, pool.workingPool.size());
        assertEquals(0, pool.idlePool.size());
    }

    @Test
    public void testTakeFromIdlePoolReturnsIdleSession() throws Exception {
        NGServer server = new NGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        // First take creates a session
        NGSession session1 = pool.take();
        // Give it back to idle pool
        pool.give(session1);
        
        // Take again should get the same session from idle pool
        NGSession session2 = pool.take();
        assertSame(session1, session2);
        assertTrue(pool.workingPool.contains(session1));
        assertEquals(1, pool.workingPool.size());
        assertEquals(0, pool.idlePool.size());
    }

    @Test
    public void testGiveToIdlePoolWhenUnderLimit() throws Exception {
        NGServer server = new NGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        NGSession session = pool.take();
        pool.give(session);
        
        assertEquals(0, pool.workingPool.size());
        assertEquals(1, pool.idlePool.size());
        assertSame(session, pool.idlePool.peek());
    }

    @Test
    public void testGiveToShutdownWhenIdlePoolFull() throws Exception {
        NGServer server = new NGServer();
        NGSessionPool pool = new NGSessionPool(server, 1);
        
        // Take and give back two sessions to fill the idle pool
        NGSession session1 = pool.take();
        pool.give(session1);
        NGSession session2 = pool.take();
        pool.give(session2);
        
        // Now pool is full, giving another session should shutdown it
        NGSession session3 = pool.take();
        pool.give(session3);
        
        assertFalse(session3.isAlive());
    }

    @Test
    public void testShutdownWithWorkingAndIdleSessions() throws Exception {
        NGServer server = new NGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        // Take and give some sessions
        NGSession session1 = pool.take();
        NGSession session2 = pool.take();
        pool.give(session1);
        pool.give(session2);
        
        // Shutdown the pool
        pool.shutdown();
        
        // All sessions should be shutdown
        assertFalse(session1.isAlive());
        assertFalse(session2.isAlive());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTakeAfterShutdown() throws Exception {
        NGServer server = new NGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        pool.shutdown();
        pool.take();
    }

    @Test
    public void testGiveAfterShutdownDoesNothing() throws Exception {
        NGServer server = new NGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        NGSession session = pool.take();
        pool.shutdown();
        pool.give(session); // Should not throw exception
        // Session should still be in working pool since shutdown was called
        assertTrue(pool.workingPool.contains(session));
    }

    @Test
    public void testConstructorWithCustomInstanceCreator() {
        NGServer server = new NGServer();
        AtomicBoolean created = new AtomicBoolean(false);
        Supplier<NGSession> creator = () -> {
            created.set(true);
            return new NGSession(null, server);
        };
        NGSessionPool pool = new NGSessionPool(server, 2, creator);
        assertEquals(2, pool.maxIdleSessions);
        assertTrue(created.get());
    }
}
