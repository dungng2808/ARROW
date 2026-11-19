package org.springframework.retry.backoff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.backoff.Sleeper;

public class FixedBackOffPolicy_ArrowTest {

	@Test
	public void testDefaultBackOffPeriod() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		assertEquals(1000L, policy.getBackOffPeriod());
	}

	@Test
	public void testSetBackOffPeriod() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(5000L);
		assertEquals(5000L, policy.getBackOffPeriod());
	}

	@Test
	public void testSetBackOffPeriodLessThanOne() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(0L);
		assertEquals(1L, policy.getBackOffPeriod());
	}

	@Test
	public void testSetBackOffPeriodNegative() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(-1000L);
		assertEquals(1L, policy.getBackOffPeriod());
	}

	@Test
	public void testBackOffPeriodSupplier() {
		AtomicLong counter = new AtomicLong(1000L);
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.backOffPeriodSupplier(counter::get);
		assertEquals(1000L, policy.getBackOffPeriod());
		counter.set(2000L);
		assertEquals(2000L, policy.getBackOffPeriod());
	}

	@Test
	public void testSetSleeper() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		Sleeper sleeper = mock(Sleeper.class);
		policy.setSleeper(sleeper);
		assertEquals(sleeper, policy.getClass().getDeclaredField("sleeper").get(policy));
	}

	@Test
	public void testToString() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(2000L);
		String result = policy.toString();
		assertNotNull(result);
		assertNotEquals("", result);
	}

	@Test
	public void testWithSleeper() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		Sleeper sleeper = mock(Sleeper.class);
		FixedBackOffPolicy result = policy.withSleeper(sleeper);
		assertNotNull(result);
		assertNotEquals(policy, result);
	}
}
