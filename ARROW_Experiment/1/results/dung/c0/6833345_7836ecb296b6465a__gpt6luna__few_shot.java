package com.facebook.nailgun;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class NGSessionPool_ArrowTest {

  @Test
  public void constructorClampsNegativeIdleCapacityToZero() {
    NGSessionPool pool = new NGSessionPool(null, -1);
    assertEquals(0, pool.maxIdleSessions);
    assertTrue(pool.idlePool.isEmpty());
    assertTrue(pool.workingPool.isEmpty());
    assertFalse(pool.done);
  }

  @Test
  public void takeCreatesAndTracksSession() {
    final AtomicInteger creations = new AtomicInteger();
    NGSessionPool pool =
        new NGSessionPool(
            null,
            1,
            () -> {
              creations.incrementAndGet();
              return new NGSession(poolReference[0], null);
            });
    poolReference[0] = pool;

    NGSession session = pool.take();

    assertNotNull(session);
    assertEquals(1, creations.get());
    assertTrue(pool.workingPool.contains(session));
    assertTrue(pool.idlePool.isEmpty());
    pool.shutdown();
  }

  private static final NGSessionPool[] poolReference = new NGSessionPool[1];

  @Test
  public void giveReturnsWorkingSessionToIdlePool() throws InterruptedException {
    NGSessionPool pool = new NGSessionPool(null, 1);
    NGSession session = pool.take();

    pool.give(session);

    assertTrue(pool.workingPool.isEmpty());
    assertEquals(1, pool.idlePool.size());
    assertSame(session, pool.idlePool.peek());
    pool.shutdown();
  }

  @Test
  public void giveShutsDownSessionWhenIdlePoolIsFull() throws InterruptedException {
    NGSessionPool pool = new NGSessionPool(null, 0);
    NGSession session = pool.take();

    pool.give(session);

    assertTrue(pool.workingPool.isEmpty());
    assertTrue(pool.idlePool.isEmpty());
    assertFalse(session.isAlive());
    pool.shutdown();
  }

  @Test
  public void shutdownMarksPoolDoneAndClearsSessions() throws InterruptedException {
    NGSessionPool pool = new NGSessionPool(null, 1);
    NGSession session = pool.take();
    pool.give(session);

    pool.shutdown();

    assertTrue(pool.done);
    assertTrue(pool.idlePool.isEmpty());
    assertTrue(pool.workingPool.isEmpty());
    assertFalse(session.isAlive());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void takeAfterShutdownThrows() throws InterruptedException {
    NGSessionPool pool = new NGSessionPool(null, 0);
    pool.shutdown();
    pool.take();
  }
}
