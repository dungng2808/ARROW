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
    
    // Add a session to idle pool
    NGSession idleSession = mock(NGSession.class);
    pool.idlePool.add(idleSession);
    
    NGSession takenSession = pool.take();
    
    assertEquals(idleSession, takenSession);
    assertTrue(pool.workingPool.contains(takenSession));
    assertFalse(pool.idlePool.contains(takenSession));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testTakeAfterShutdown() {
    pool = new NGSessionPool(server, 2, mockInstanceCreator);
    pool.shutdown();
    
    pool.take();
  }

  @Test
  public void testGiveWhenIdlePoolNotFull() {
    pool = new NGSessionPool(server, 2, mockInstanceCreator);
    
    NGSession session = pool.take();
    
    pool.give(session);
    
    assertTrue(pool.idlePool.contains(session));
    assertFalse(pool.workingPool.contains(session));
  }

  @Test
  public void testGiveWhenIdlePoolFull() throws InterruptedException {
    pool = new NGSessionPool(server, 1, mockInstanceCreator);
    
    // Take and give back one session to fill the idle pool
    NGSession session1 = pool.take();
    pool.give(session1);
    
    // Take another session (this will be created fresh)
    NGSession session2 = pool.take();
    
    // Give back the second session - should be shutdown since idle pool is full
    pool.give(session2);
    
    verify(session2, times(1)).shutdown();
  }

  @Test
  public void testShutdown() throws InterruptedException {
    pool = new NGSessionPool(server, 2, mockInstanceCreator);
    
    // Take a session to add it to working pool
    NGSession session1 = pool.take();
    
    // Add a session to idle pool
    NGSession session2 = mock(NGSession.class);
    pool.idlePool.add(session2);
    
    pool.shutdown();
    
    verify(session1, times(1)).shutdown();
    verify(session2, times(1)).shutdown();
    assertTrue(pool.done);
  }

  @Test
  public void testShutdownWithRunningSessions() throws InterruptedException {
    pool = new NGSessionPool(server, 1, mockInstanceCreator);
    
    // Mock session to simulate being alive
    when(mockSession.isAlive()).thenReturn(true);
    when(mockSession.join(1000L)).thenAnswer(invocation -> {
      // Simulate some time passing
      Thread.sleep(10);
      return null;
    });
    
    NGSession session = pool.take();
    
    pool.shutdown();
    
    // Should not throw exception even though session is alive
    // (This test verifies that the shutdown doesn't crash)
  }
}
