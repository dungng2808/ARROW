package com.facebook.nailgun;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class NGSessionPool_ArrowTest {

  @Test
  public void takeCreatesAndTracksSession() {
    final AtomicInteger creations = new AtomicInteger();
    NGSessionPool pool =
        new NGSessionPool(null, 1, () -> {
          creations.incrementAndGet();
          return new NGSession(poolReference[0], null);
        });
  }

  private final NGSessionPool[] poolReference = new NGSessionPool[1];

  @Test
  public void constructorClampsNegativeIdleCapacity() {
    NGSessionPool pool = new NGSessionPool(null, -3, () -> null);
    assertEquals(0, pool.maxIdleSessions);
    assertTrue(pool.idlePool.isEmpty());
    assertTrue(pool.workingPool.isEmpty());
  }

  @Test
  public void giveStoresSessionWhenCapacityAvailable() {
    NGSessionPool pool = new NGSessionPool(null, 1, () -> null);
    NGSession session = new NGSession(pool, null);
    pool.give(session);
    assertEquals(1, pool.idlePool.size());
    assertTrue(pool.idlePool.contains(session));
    assertTrue(pool.workingPool.isEmpty());
  }

  @Test
  public void giveRemovesWorkingSessionWhenIdleCapacityIsFull() {
    NGSessionPool pool = new NGSessionPool(null, 0, () -> null);
    NGSession session = new NGSession(pool, null);
    pool.workingPool.add(session);
    pool.give(session);
    assertTrue(pool.workingPool.isEmpty());
    assertTrue(pool.idlePool.isEmpty());
  }

  @Test
  public void giveDoesNothingAfterShutdown() throws InterruptedException {
    NGSessionPool pool = new NGSessionPool(null, 0, () -> null);
    pool.shutdown();
    NGSession session = new NGSession(pool, null);
    pool.give(session);
    assertTrue(pool.idlePool.isEmpty());
    assertTrue(pool.workingPool.isEmpty());
  }

  @Test
  public void takeAfterShutdownThrows() throws InterruptedException {
    NGSessionPool pool = new NGSessionPool(null, 0, () -> null);
    pool.shutdown();
    try {
      pool.take();
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException expected) {
      assertEquals("NGSession pool is shutting down", expected.getMessage());
    }
  }

  @Test
  public void shutdownMarksPoolDoneAndClearsCollections() throws InterruptedException {
    NGSessionPool pool = new NGSessionPool(null, 1, () -> null);
    pool.shutdown();
    assertTrue(pool.done);
    assertTrue(pool.idlePool.isEmpty());
    assertTrue(pool.workingPool.isEmpty());
  }
}
