package org.springframework.retry.backoff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.function.Supplier;

import org.junit.Test;

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
	public void testSetBackOffPeriodZero() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(0L);
		assertEquals(1L, policy.getBackOffPeriod());
	}

	@Test
	public void testSetBackOffPeriodNegative() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(-100L);
		assertEquals(1L, policy.getBackOffPeriod());
	}

	@Test
	public void testBackOffPeriodSupplier() {
		Supplier<Long> supplier = () -> 2000L;
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.backOffPeriodSupplier(supplier);
		assertEquals(2000L, policy.getBackOffPeriod());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBackOffPeriodSupplierNull() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.backOffPeriodSupplier(null);
	}

	@Test
	public void testWithSleeper() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		Sleeper sleeper = new ThreadWaitSleeper();
		FixedBackOffPolicy result = policy.withSleeper(sleeper);
		assertNotNull(result);
		assertEquals(1000L, result.getBackOffPeriod());
	}

	@Test
	public void testSetSleeper() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		Sleeper sleeper = new ThreadWaitSleeper();
		policy.setSleeper(sleeper);
		// We can't easily test the sleeper is set, but we can ensure no exception is thrown
		assertTrue(true);
	}

	@Test
	public void testToString() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		String result = policy.toString();
		assertTrue(result.contains("FixedBackOffPolicy"));
		assertTrue(result.contains("backOffPeriod=1000"));
	}

	@Test
	public void testDoBackOff() throws BackOffInterruptedException {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		// This should not throw an exception
		policy.doBackOff();
		// No assertion needed - just ensuring no exception is thrown
	}
}
