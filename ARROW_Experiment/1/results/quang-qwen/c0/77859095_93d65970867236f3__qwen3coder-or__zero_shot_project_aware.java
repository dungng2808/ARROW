package io.trane.future;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Test;
import static org.junit.Assert.*;

public class NoFuture_ArrowTest {

    @Test
    public void testRaise() {
        NoFuture<String> future = new NoFuture<>();
        future.raise(new RuntimeException("test"));
        // No operation, just verify it compiles and runs
    }

    @Test
    public void testInterruptible() {
        NoFuture<String> future = new NoFuture<>();
        Future<String> result = future.interruptible();
        assertSame(future, result);
    }

    @Test
    public void testMap() {
        NoFuture<String> future = new NoFuture<>();
        Future<Integer> result = future.map(String::length);
        assertNotNull(result);
        assertTrue(result instanceof NoFuture);
    }

    @Test
    public void testFlatMap() {
        NoFuture<String> future = new NoFuture<>();
        Future<Integer> result = future.flatMap(s -> new NoFuture<>());
        assertNotNull(result);
        assertTrue(result instanceof NoFuture);
    }

    @Test
    public void testTransform() {
        NoFuture<String> future = new NoFuture<>();
        Future<Integer> result = future.transform((f, c) -> 42);
        assertNotNull(result);
        assertTrue(result instanceof NoFuture);
    }

    @Test
    public void testTransformWith() {
        NoFuture<String> future = new NoFuture<>();
        Future<Integer> result = future.transformWith((f, c) -> new NoFuture<>());
        assertNotNull(result);
        assertTrue(result instanceof NoFuture);
    }

    @Test
    public void testBiMap() {
        NoFuture<String> future = new NoFuture<>();
        NoFuture<Integer> other = new NoFuture<>();
        Future<String> result = future.biMap(other, (s, i) -> s);
        assertNotNull(result);
        assertTrue(result instanceof NoFuture);
    }

    @Test
    public void testBiFlatMap() {
        NoFuture<String> future = new NoFuture<>();
        NoFuture<Integer> other = new NoFuture<>();
        Future<String> result = future.biFlatMap(other, (s, i) -> new NoFuture<>());
        assertNotNull(result);
        assertTrue(result instanceof NoFuture);
    }

    @Test
    public void testEnsure() {
        NoFuture<String> future = new NoFuture<>();
        Future<String> result = future.ensure(() -> {});
        assertSame(future, result);
    }

    @Test
    public void testOnSuccess() {
        NoFuture<String> future = new NoFuture<>();
        Future<String> result = future.onSuccess(s -> {});
        assertSame(future, result);
    }

    @Test
    public void testOnFailure() {
        NoFuture<String> future = new NoFuture<>();
        Future<String> result = future.onFailure(t -> {});
        assertSame(future, result);
    }

    @Test
    public void testRespond() {
        NoFuture<String> future = new NoFuture<>();
        Future<String> result = future.respond((t, c) -> {});
        assertSame(future, result);
    }

    @Test
    public void testRescue() {
        NoFuture<String> future = new NoFuture<>();
        Future<String> result = future.rescue(t -> new NoFuture<>());
        assertSame(future, result);
    }

    @Test
    public void testIsDefined() {
        NoFuture<String> future = new NoFuture<>();
        assertFalse(future.isDefined());
    }

    @Test(expected = TimeoutException.class)
    public void testGet() throws CheckedFutureException {
        NoFuture<String> future = new NoFuture<>();
        future.get(Duration.ofMillis(10));
    }

    @Test
    public void testJoin() throws CheckedFutureException {
        NoFuture<String> future = new NoFuture<>();
        future.join(Duration.ofMillis(10));
        // Should complete without exception
    }

    @Test
    public void testVoided() {
        NoFuture<String> future = new NoFuture<>();
        Future<Void> result = future.voided();
        assertNotNull(result);
        assertTrue(result instanceof NoFuture);
    }

    @Test
    public void testDelayed() {
        NoFuture<String> future = new NoFuture<>();
        ScheduledExecutorService scheduler = null; // We don't need a real scheduler for this test
        Future<String> result = future.delayed(Duration.ofMillis(10), scheduler);
        assertSame(future, result);
    }

    @Test
    public void testProxyTo() {
        NoFuture<String> future = new NoFuture<>();
        Promise<String> promise = new Promise<>();
        future.proxyTo(promise);
        // No operation, just verify it compiles and runs
    }

    @Test
    public void testWithin() {
        NoFuture<String> future = new NoFuture<>();
        ScheduledExecutorService scheduler = null; // We don't need a real scheduler for this test
        Future<String> result = future.within(Duration.ofMillis(10), scheduler, new RuntimeException());
        assertSame(future, result);
    }
}
