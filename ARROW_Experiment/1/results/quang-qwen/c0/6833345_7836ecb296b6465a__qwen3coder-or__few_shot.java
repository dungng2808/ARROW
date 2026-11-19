package com.facebook.nailgun;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;

public class NGSessionPool_ArrowTest {

  private NGServer server;
  private NGSessionPool pool;
  private Supplier<NGSession> mockInstanceCreator;
  private NGSession mockSession;

  @Before
  public void setUp() {
    server = mock(NGServer.class);
    mockSession = mock(NGSession.class);
    mockInstanceCreator = () -> mockSession;
  }

  @Test
  public void testConstructorWithZeroMaxIdle() {
    pool = new NGSessionPool(server, 0, mockInstanceCreator);
    assertEquals(0, pool.maxIdleSessions);
  }

  @Test
  public void testConstructorWithNegativeMaxIdle() {
    pool = new NGSessionPool(server, -5, mockInstanceCreator);
    assertEquals(0, pool.maxIdleSessions);
  }

  @Test
  public void testConstructorWithPositiveMaxIdle() {
    pool = new NGSessionPool(server, 5, mockInstanceCreator);
    assertEquals(5, pool.maxIdleSessions);
  }

  @Test
  public void testTakeWhenPoolIsEmpty() {
    pool = new NGSessionPool(server, 2, mockInstanceCreator);
    
    NGSession session = pool.take();
    
    assertEquals(mockSession, session);
    assertTrue(pool.workingPool.contains(session));
    assertFalse(pool.idlePool.contains(session));
  }

  @Test
  public void testTakeWhenPoolHasIdleSessions() {
    pool = new NGSessionPool(server, 2, mockInstanceCreator);
    
    // Add a session to the idle pool
    pool.idlePool.offer(mockSession);
    when(mockSession.isAlive()).thenReturn(true);
    
    NGSession session = pool.take();
    
    assertEquals(mockSession, session);
    assertTrue(pool.workingPool.contains(session));
    assertFalse(pool.idlePool.contains(session));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testTakeWhenPoolIsShutdown() {
    pool = new NGSessionPool(server, 2, mockInstanceCreator);
    pool.done = true;
    
    pool.take();
  }

  @Test
  public void testGiveWhenIdlePoolNotFull() {
    pool = new NGSessionPool(server, 2, mockInstanceCreator);
    
    // Add a session to the working pool
    pool.workingPool.add(mockSession);
    
    pool.give(mockSession);
    
    assertTrue(pool.idlePool.contains(mockSession));
    assertFalse(pool.workingPool.contains(mockSession));
  }

  @Test
  public void testGiveWhenIdlePoolIsFull() {
    pool = new NGSessionPool(server, 1, mockInstanceCreator);
    
    // Add a session to the idle pool
    pool.idlePool.offer(mockSession);
    
    // Add another session to the working pool
    NGSession secondSession = mock(NGSession.class);
    pool.workingPool.add(secondSession);
    
    pool.give(secondSession);
    
    assertFalse(pool.idlePool.contains(secondSession));
    assertFalse(pool.workingPool.contains(secondSession));
    verify(secondSession, times(1)).shutdown();
  }

  @Test
  public void testShutdown() throws InterruptedException {
    pool = new NGSessionPool(server, 2, mockInstanceCreator);
    
    // Add sessions to both pools
    NGSession workingSession = mock(NGSession.class);
    NGSession idleSession = mock(NGSession.class);
    pool.workingPool.add(workingSession);
    pool.idlePool.offer(idleSession);
    
    when(workingSession.isAlive()).thenReturn(false);
    when(idleSession.isAlive()).thenReturn(false);
    
    pool.shutdown();
    
    assertTrue(pool.done);
    assertTrue(pool.idlePool.isEmpty());
    assertTrue(pool.workingPool.isEmpty());
    verify(workingSession, times(1)).shutdown();
    verify(idleSession, times(1)).shutdown();
  }

  @Test
  public void testShutdownWithTimeout() throws InterruptedException {
    pool = new NGSessionPool(server, 2, mockInstanceCreator);
    
    // Add sessions to both pools
    NGSession workingSession = mock(NGSession.class);
    NGSession idleSession = mock(NGSession.class);
    pool.workingPool.add(workingSession);
    pool.idlePool.offer(idleSession);
    
    when(workingSession.isAlive()).thenReturn(true);
    when(idleSession.isAlive()).thenReturn(true);
    
    // Mock join to simulate timeout
    when(workingSession.join(NGConstants.SESSION_TERMINATION_TIMEOUT_MILLIS))
        .thenAnswer(invocation -> {
          Thread.sleep(10);
          return null;
        });
    
    try {
      pool.shutdown();
    } catch (IllegalStateException e) {
      // Expected due to timeout
    }
  }
}
