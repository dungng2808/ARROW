package io.trane.future;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
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
        // Method does nothing, so no assertion needed
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
        assertSame(future, result);
    }

    @Test
    public void testFlatMap() {
        NoFuture<String> future = new NoFuture<>();
        Future<Integer> result = future.flatMap(s -> new NoFuture<>());
        assertSame(future, result);
    }

    @Test
    public void testTransform() {
        NoFuture<String> future = new NoFuture<>();
        Future<Integer> result = future.transform((t, c) -> 42);
        assertSame(future, result);
    }

    @Test
    public void testTransformWith() {
        NoFuture<String> future = new NoFuture<>();
        Future<Integer> result = future.transformWith((t, c) -> new NoFuture<>());
        assertSame(future, result);
    }

    @Test
    public void testBiMap() {
        NoFuture<String> future = new NoFuture<>();
        NoFuture<Integer> other = new NoFuture<>();
        Future<String> result = future.biMap(other, (s, i) -> s);
        assertSame(future, result);
    }

    @Test
    public void testBiFlatMap() {
        NoFuture<String> future = new NoFuture<>();
        NoFuture<Integer> other = new NoFuture<>();
        Future<String> result = future.biFlatMap(other, (s, i) -> new NoFuture<>());
        assertSame(future, result);
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
        // Should not throw anything
    }

    @Test
    public void testVoided() {
        NoFuture<String> future = new NoFuture<>();
        Future<Void> result = future.voided();
        assertSame(future, result);
    }

    @Test
    public void testDelayed() {
        NoFuture<String> future = new NoFuture<>();
        Future<String> result = future.delayed(Duration.ofMillis(10), null);
        assertSame(future, result);
    }

    @Test
    public void testProxyTo() {
        NoFuture<String> future = new NoFuture<>();
        Promise<String> promise = new Promise<>();
        future.proxyTo(promise);
        // Method does nothing, so no assertion needed
    }

    @Test
    public void testWithin() {
        NoFuture<String> future = new NoFuture<>();
        Future<String> result = future.within(Duration.ofMillis(10), null, new RuntimeException());
        assertSame(future, result);
    }
}
