package com.facebook.nailgun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class NGSessionPool_ArrowTest {

  @Test
  public void takeCreatesAndTracksSession() {
    AtomicInteger creations = new AtomicInteger();
    NGSessionPool pool =
        new NGSessionPool(null, 2, () -> {
          creations.incrementAndGet();
          return new NGSession(poolReference.get(), null);
        });
  }

  private static final ThreadLocal<NGSessionPool> poolReference = new ThreadLocal<>();

  @Test
  public void constructorClampsNegativeIdleCapacity() {
    NGSessionPool pool = new NGSessionPool(null, -1, () -> null);
    assertEquals(0, pool.maxIdleSessions);
    assertTrue(pool.idlePool.isEmpty());
    assertTrue(pool.workingPool.isEmpty());
  }

  @Test
  public void giveStoresSessionWhenCapacityIsAvailable() {
    NGSessionPool pool = new NGSessionPool(null, 1, () -> null);
    NGSession session = new NGSession(pool, null);
    pool.give(session);
    assertEquals(1, pool.idlePool.size());
    assertSame(session, pool.idlePool.peek());
  }

  @Test
  public void giveDiscardsSessionWhenIdlePoolIsFull() {
    NGSessionPool pool = new NGSessionPool(null, 0, () -> null);
    NGSession session = new NGSession(pool, null);
    pool.give(session);
    assertTrue(pool.idlePool.isEmpty());
    assertTrue(pool.workingPool.isEmpty());
  }

  @Test
  public void takeAfterShutdownThrows() throws InterruptedException {
    NGSessionPool pool = new NGSessionPool(null, 0, () -> null);
    pool.shutdown();
    assertThrows(UnsupportedOperationException.class, pool::take);
  }

  @Test
  public void giveAfterShutdownDoesNothing() throws InterruptedException {
    NGSessionPool pool = new NGSessionPool(null, 1, () -> null);
    pool.shutdown();
    NGSession session = new NGSession(pool, null);
    pool.give(session);
    assertTrue(pool.idlePool.isEmpty());
    assertTrue(pool.workingPool.isEmpty());
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
