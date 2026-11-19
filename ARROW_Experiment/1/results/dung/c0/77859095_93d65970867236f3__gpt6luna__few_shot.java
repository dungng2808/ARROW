package io.trane.future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.time.Duration;
import org.junit.Test;

public class NoFuture_ArrowTest {

  @Test
  public void isNeverDefined() {
    NoFuture<String> future = new NoFuture<String>();
    assertFalse(future.isDefined());
  }

  @Test
  public void transformationsReturnTheSameFuture() {
    NoFuture<String> future = new NoFuture<String>();

    assertSame(future, future.interruptible());
    assertSame(future, future.map(value -> value.length()));
    assertSame(future, future.flatMap(value -> null));
    assertSame(future, future.transform(null));
    assertSame(future, future.transformWith(null));
    assertSame(future, future.biMap(null, (left, right) -> null));
    assertSame(future, future.biFlatMap(null, (left, right) -> null));
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
  public void getAlwaysTimesOutAfterJoining() {
    NoFuture<String> future = new NoFuture<String>();
    assertThrows(TimeoutException.class, () -> future.get(Duration.ZERO));
  }

  @Test
  public void joinReturnsAfterTheRequestedDuration() throws Exception {
    NoFuture<String> future = new NoFuture<String>();
    future.join(Duration.ZERO);
  }

  @Test
  public void raiseAndProxyToDoNothing() {
    NoFuture<String> future = new NoFuture<String>();
    future.raise(new RuntimeException());
    future.proxyTo(null);
  }
}
