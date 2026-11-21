package com.facebook.nailgun;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class NGSessionPool_ArrowTest {

    @Test
    public void testConstructorWithMaxIdleZero() {
        NGServer server = new MockNGServer();
        NGSessionPool pool = new NGSessionPool(server, 0);
        assertEquals(0, pool.maxIdleSessions);
    }

    @Test
    public void testConstructorWithNegativeMaxIdle() {
        NGServer server = new MockNGServer();
        NGSessionPool pool = new NGSessionPool(server, -5);
        assertEquals(0, pool.maxIdleSessions);
    }

    @Test
    public void testConstructorWithPositiveMaxIdle() {
        NGServer server = new MockNGServer();
        NGSessionPool pool = new NGSessionPool(server, 5);
        assertEquals(5, pool.maxIdleSessions);
    }

    @Test
    public void testTakeWhenPoolEmpty() throws Exception {
        NGServer server = new MockNGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        NGSession session = pool.take();
        assertNotNull(session);
        assertTrue(pool.workingPool.contains(session));
        assertEquals(1, pool.workingPool.size());
        assertEquals(0, pool.idlePool.size());
    }

    @Test
    public void testTakeWhenPoolHasIdleSessions() throws Exception {
        NGServer server = new MockNGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        // Add a session to idle pool
        NGSession idleSession = new NGSession(pool, server);
        pool.idlePool.add(idleSession);
        
        NGSession takenSession = pool.take();
        assertSame(idleSession, takenSession);
        assertTrue(pool.workingPool.contains(takenSession));
        assertEquals(1, pool.workingPool.size());
        assertEquals(0, pool.idlePool.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTakeAfterShutdown() throws Exception {
        NGServer server = new MockNGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        pool.shutdown();
        pool.take();
    }

    @Test
    public void testGiveWhenIdlePoolNotFull() throws Exception {
        NGServer server = new MockNGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        NGSession session = pool.take();
        pool.give(session);
        
        assertTrue(pool.idlePool.contains(session));
        assertFalse(pool.workingPool.contains(session));
        assertEquals(1, pool.idlePool.size());
        assertEquals(0, pool.workingPool.size());
    }

    @Test
    public void testGiveWhenIdlePoolFull() throws Exception {
        NGServer server = new MockNGServer();
        NGSessionPool pool = new NGSessionPool(server, 1);
        
        // Take and give a session to fill the idle pool
        NGSession session1 = pool.take();
        pool.give(session1);
        
        // Take another session, this should not go to idle pool
        NGSession session2 = pool.take();
        pool.give(session2);
        
        // session2 should be shutdown since idle pool is full
        assertTrue(session2.isShutdownCalled());
        assertFalse(pool.idlePool.contains(session2));
        assertFalse(pool.workingPool.contains(session2));
        assertEquals(1, pool.idlePool.size());
    }

    @Test
    public void testShutdownWithNoSessions() throws Exception {
        NGServer server = new MockNGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        pool.shutdown();
        assertTrue(pool.done);
    }

    @Test
    public void testShutdownWithWorkingAndIdleSessions() throws Exception {
        NGServer server = new MockNGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        
        // Take and give some sessions
        NGSession session1 = pool.take();
        NGSession session2 = pool.take();
        pool.give(session1);
        pool.give(session2);
        
        pool.shutdown();
        
        assertTrue(pool.done);
        assertEquals(0, pool.workingPool.size());
        assertEquals(0, pool.idlePool.size());
    }

    @Test
    public void testGiveAfterShutdown() throws Exception {
        NGServer server = new MockNGServer();
        NGSessionPool pool = new NGSessionPool(server, 2);
        pool.shutdown();
        
        NGSession session = new MockNGSession();
        pool.give(session); // Should not throw exception
    }

    // Helper classes for testing
    private static class MockNGServer extends NGServer {
        @Override
        public void run() {
            // No-op
        }
    }

    private static class MockNGSession extends NGSession {
        private AtomicBoolean shutdownCalled = new AtomicBoolean(false);
        
        public MockNGSession() {
            super(null, null);
        }
        
        @Override
        public void shutdown() {
            shutdownCalled.set(true);
        }
        
        public boolean isShutdownCalled() {
            return shutdownCalled.get();
        }
    }
}
