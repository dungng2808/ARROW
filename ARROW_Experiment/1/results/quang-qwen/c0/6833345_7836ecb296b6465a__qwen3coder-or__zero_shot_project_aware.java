package com.facebook.nailgun;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class NGSessionPool_ArrowTest {

    private NGServer mockServer;
    private NGSessionPool pool;
    private Supplier<NGSession> mockInstanceCreator;

    @BeforeEach
    public void setUp() {
        mockServer = mock(NGServer.class);
        mockInstanceCreator = mock(Supplier.class);
    }

    @Test
    public void testConstructorWithMaxIdleZero() {
        pool = new NGSessionPool(mockServer, 0, mockInstanceCreator);
        assertEquals(0, pool.maxIdleSessions);
    }

    @Test
    public void testConstructorWithNegativeMaxIdle() {
        pool = new NGSessionPool(mockServer, -5, mockInstanceCreator);
        assertEquals(0, pool.maxIdleSessions);
    }

    @Test
    public void testConstructorWithPositiveMaxIdle() {
        pool = new NGSessionPool(mockServer, 5, mockInstanceCreator);
        assertEquals(5, pool.maxIdleSessions);
    }

    @Test
    public void testTakeFromEmptyPoolCreatesNewSession() throws Exception {
        pool = new NGSessionPool(mockServer, 2, mockInstanceCreator);
        NGSession mockSession = mock(NGSession.class);
        when(mockInstanceCreator.get()).thenReturn(mockSession);

        NGSession result = pool.take();

        assertSame(mockSession, result);
        assertTrue(pool.workingPool.contains(mockSession));
        assertEquals(1, pool.workingPool.size());
        assertEquals(0, pool.idlePool.size());
    }

    @Test
    public void testTakeFromIdlePoolReturnsIdleSession() throws Exception {
        pool = new NGSessionPool(mockServer, 2, mockInstanceCreator);
        NGSession mockSession1 = mock(NGSession.class);
        NGSession mockSession2 = mock(NGSession.class);
        pool.idlePool.add(mockSession1);
        pool.idlePool.add(mockSession2);

        NGSession result1 = pool.take();
        NGSession result2 = pool.take();

        assertSame(mockSession1, result1);
        assertSame(mockSession2, result2);
        assertTrue(pool.workingPool.contains(mockSession1));
        assertTrue(pool.workingPool.contains(mockSession2));
        assertEquals(2, pool.workingPool.size());
        assertEquals(0, pool.idlePool.size());
    }

    @Test
    public void testGiveToPoolWhenBelowMaxIdle() throws Exception {
        pool = new NGSessionPool(mockServer, 2, mockInstanceCreator);
        NGSession mockSession = mock(NGSession.class);
        pool.workingPool.add(mockSession);

        pool.give(mockSession);

        assertTrue(pool.idlePool.contains(mockSession));
        assertFalse(pool.workingPool.contains(mockSession));
        assertEquals(1, pool.idlePool.size());
        assertEquals(0, pool.workingPool.size());
    }

    @Test
    public void testGiveToPoolWhenAtMaxIdle() throws Exception {
        pool = new NGSessionPool(mockServer, 1, mockInstanceCreator);
        NGSession mockSession1 = mock(NGSession.class);
        NGSession mockSession2 = mock(NGSession.class);
        pool.idlePool.add(mockSession1);
        pool.workingPool.add(mockSession2);

        pool.give(mockSession2);

        assertFalse(pool.idlePool.contains(mockSession2));
        assertFalse(pool.workingPool.contains(mockSession2));
        assertTrue(pool.idlePool.contains(mockSession1));
        assertEquals(1, pool.idlePool.size());
        assertEquals(0, pool.workingPool.size());
    }

    @Test
    public void testShutdownWithNoSessions() throws Exception {
        pool = new NGSessionPool(mockServer, 2, mockInstanceCreator);
        pool.shutdown();
        // Should not throw any exception
    }

    @Test
    public void testShutdownWithWorkingAndIdleSessions() throws Exception {
        pool = new NGSessionPool(mockServer, 2, mockInstanceCreator);
        NGSession mockSession1 = mock(NGSession.class);
        NGSession mockSession2 = mock(NGSession.class);
        pool.workingPool.add(mockSession1);
        pool.idlePool.add(mockSession2);

        pool.shutdown();

        verify(mockSession1).shutdown();
        verify(mockSession2).shutdown();
    }

    @Test
    public void testTakeAfterShutdownThrowsException() throws Exception {
        pool = new NGSessionPool(mockServer, 2, mockInstanceCreator);
        pool.shutdown();

        assertThrows(UnsupportedOperationException.class, () -> {
            pool.take();
        });
    }

    @Test
    public void testGiveAfterShutdownDoesNothing() throws Exception {
        pool = new NGSessionPool(mockServer, 2, mockInstanceCreator);
        NGSession mockSession = mock(NGSession.class);
        pool.workingPool.add(mockSession);
        pool.shutdown();

        pool.give(mockSession);

        // Should not throw any exception and should not modify anything
        assertFalse(pool.idlePool.contains(mockSession));
        assertTrue(pool.workingPool.contains(mockSession));
    }
}
