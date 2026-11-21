package io.trane.future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.time.Duration;
import org.junit.Test;

public class NoFuture_ArrowTest {

  @Test
  public void isDefinedReturnsFalse() {
    NoFuture<String> future = new NoFuture<String>();

    assertFalse(future.isDefined());
  }

  @Test
  public void interruptibleReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.interruptible());
  }

  @Test
  public void mapReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.map(value -> value.length()));
  }

  @Test
  public void flatMapReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.flatMap(value -> future));
  }

  @Test
  public void transformReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.transform(null));
  }

  @Test
  public void transformWithReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.transformWith(null));
  }

  @Test
  public void biMapReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.biMap(null, (left, right) -> left.length()));
  }

  @Test
  public void biFlatMapReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.biFlatMap(null, (left, right) -> future));
  }

  @Test
  public void ensureReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.ensure(null));
  }

  @Test
  public void onSuccessReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.onSuccess(null));
  }

  @Test
  public void onFailureReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.onFailure(null));
  }

  @Test
  public void respondReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.respond(null));
  }

  @Test
  public void rescueReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.rescue(null));
  }

  @Test
  public void voidedReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.voided());
  }

  @Test
  public void delayedReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.delayed(Duration.ZERO, null));
  }

  @Test
  public void withinReturnsSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.within(Duration.ZERO, null, null));
  }

  @Test
  public void getThrowsTimeoutException() throws Exception {
    NoFuture<String> future = new NoFuture<String>();

    try {
      future.get(Duration.ZERO);
      fail("Expected TimeoutException");
    } catch (TimeoutException expected) {
      // Expected.
    }
  }

  @Test
  public void raiseDoesNothing() {
    NoFuture<String> future = new NoFuture<String>();

    future.raise(new RuntimeException());
    assertFalse(future.isDefined());
  }

  @Test
  public void proxyToDoesNothing() {
    NoFuture<String> future = new NoFuture<String>();

    future.proxyTo(null);
    assertFalse(future.isDefined());
  }
}
