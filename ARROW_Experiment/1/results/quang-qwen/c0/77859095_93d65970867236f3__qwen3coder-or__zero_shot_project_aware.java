package io.trane.future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class NoFuture_ArrowTest {

  @Test
  public void testRaise() {
    final NoFuture<String> future = new NoFuture<>();
    future.raise(new RuntimeException("test"));
  }

  @Test
  public void testInterruptible() {
    final NoFuture<String> future = new NoFuture<>();
    final Future<String> result = future.interruptible();
    assertSame(future, result);
  }

  @Test
  public void testMap() {
    final NoFuture<String> future = new NoFuture<>();
    final Future<Integer> result = future.map(String::length);
    assertSame(future, result);
  }

  @Test
  public void testFlatMap() {
    final NoFuture<String> future = new NoFuture<>();
    final Future<Integer> result = future.flatMap(s -> new NoFuture<>());
    assertSame(future, result);
  }

  @Test
  public void testTransform() {
    final NoFuture<String> future = new NoFuture<>();
    final Future<Integer> result = future.transform((f, c) -> 42);
    assertSame(future, result);
  }

  @Test
  public void testTransformWith() {
    final NoFuture<String> future = new NoFuture<>();
    final Future<Integer> result = future.transformWith((f, c) -> new NoFuture<>());
    assertSame(future, result);
  }

  @Test
  public void testBiMap() {
    final NoFuture<String> future = new NoFuture<>();
    final NoFuture<Integer> other = new NoFuture<>();
    final Future<String> result = future.biMap(other, (s, i) -> s);
    assertSame(future, result);
  }

  @Test
  public void testBiFlatMap() {
    final NoFuture<String> future = new NoFuture<>();
    final NoFuture<Integer> other = new NoFuture<>();
    final Future<String> result = future.biFlatMap(other, (s, i) -> new NoFuture<>());
    assertSame(future, result);
  }

  @Test
  public void testEnsure() {
    final NoFuture<String> future = new NoFuture<>();
    final Future<String> result = future.ensure(() -> {});
    assertSame(future, result);
  }

  @Test
  public void testOnSuccess() {
    final NoFuture<String> future = new NoFuture<>();
    final Future<String> result = future.onSuccess(s -> {});
    assertSame(future, result);
  }

  @Test
  public void testOnFailure() {
    final NoFuture<String> future = new NoFuture<>();
    final Future<String> result = future.onFailure(t -> {});
    assertSame(future, result);
  }

  @Test
  public void testRespond() {
    final NoFuture<String> future = new NoFuture<>();
    final Future<String> result = future.respond((s, t) -> {});
    assertSame(future, result);
  }

  @Test
  public void testRescue() {
    final NoFuture<String> future = new NoFuture<>();
    final Future<String> result = future.rescue(t -> new NoFuture<>());
    assertSame(future, result);
  }

  @Test
  public void testIsDefined() {
    final NoFuture<String> future = new NoFuture<>();
    assertFalse(future.isDefined());
  }

  @Test(expected = TimeoutException.class)
  public void testGet() throws CheckedFutureException {
    final NoFuture<String> future = new NoFuture<>();
    future.get(Duration.ofMillis(10));
  }

  @Test
  public void testJoin() throws CheckedFutureException {
    final NoFuture<String> future = new NoFuture<>();
    future.join(Duration.ofMillis(10));
  }

  @Test
  public void testVoided() {
    final NoFuture<String> future = new NoFuture<>();
    final Future<Void> result = future.voided();
    assertSame(future, result);
  }

  @Test
  public void testDelayed() {
    final NoFuture<String> future = new NoFuture<>();
    final ScheduledExecutorService scheduler = null;
    final Future<String> result = future.delayed(Duration.ofMillis(10), scheduler);
    assertSame(future, result);
  }

  @Test
  public void testProxyTo() {
    final NoFuture<String> future = new NoFuture<>();
    final Promise<String> promise = new Promise<>();
    future.proxyTo(promise);
  }

  @Test
  public void testWithin() {
    final NoFuture<String> future = new NoFuture<>();
    final ScheduledExecutorService scheduler = null;
    final Future<String> result = future.within(Duration.ofMillis(10), scheduler, new RuntimeException());
    assertSame(future, result);
  }
}
