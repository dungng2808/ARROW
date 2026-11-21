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
  public void transformationsReturnSameFuture() {
    NoFuture<String> future = new NoFuture<String>();
    assertSame(future, future.<Integer>map(value -> value.length()));
    assertSame(future, future.<Integer>flatMap(value -> null));
    assertSame(future, future.<Integer>transform(null));
    assertSame(future, future.<Integer>transformWith(null));
    assertSame(future, future.<String, Integer>biMap(null, (left, right) -> 1));
    assertSame(future, future.<String, Integer>biFlatMap(null, (left, right) -> null));
  }

  @Test
  public void callbacksAndModifiersReturnSameFuture() {
    NoFuture<String> future = new NoFuture<String>();
    assertSame(future, future.interruptible());
    assertSame(future, future.ensure(() -> { }));
    assertSame(future, future.onSuccess(value -> { }));
    assertSame(future, future.onFailure(error -> { }));
    assertSame(future, future.respond(null));
    assertSame(future, future.rescue(error -> null));
    assertSame(future, future.voided());
    assertSame(future, future.delayed(Duration.ZERO, null));
    assertSame(future, future.within(Duration.ZERO, null, new RuntimeException()));
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
  public void joinReturnsAfterTimeout() throws Exception {
    NoFuture<String> future = new NoFuture<String>();
    future.join(Duration.ZERO);
  }

  @Test
  public void raiseAndProxyToDoNotThrow() {
    NoFuture<String> future = new NoFuture<String>();
    future.raise(new RuntimeException());
    future.proxyTo(null);
  }
}
