package org.springframework.retry.backoff;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class FixedBackOffPolicy_ArrowTest {

	@Test
	public void defaultBackOffPeriodIsOneSecond() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();

		assertEquals(1000L, policy.getBackOffPeriod());
		assertEquals("FixedBackOffPolicy[backOffPeriod=1000]", policy.toString());
	}

	@Test
	public void setBackOffPeriodUsesConfiguredValue() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(250L);

		assertEquals(250L, policy.getBackOffPeriod());
		assertEquals("FixedBackOffPolicy[backOffPeriod=250]", policy.toString());
	}

	@Test
	public void setBackOffPeriodClampsNonPositiveValuesToOne() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(0L);
		assertEquals(1L, policy.getBackOffPeriod());

		policy.setBackOffPeriod(-5L);
		assertEquals(1L, policy.getBackOffPeriod());
	}

	@Test
	public void supplierProvidesDynamicBackOffPeriod() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		AtomicLong period = new AtomicLong(10L);
		Supplier<Long> supplier = period::get;
		policy.backOffPeriodSupplier(supplier);

		assertEquals(10L, policy.getBackOffPeriod());
		period.set(20L);
		assertEquals(20L, policy.getBackOffPeriod());
	}

	@Test
	public void withSleeperReturnsSeparatePolicyWithSamePeriod() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(75L);
		Sleeper sleeper = millis -> { };
		FixedBackOffPolicy copy = policy.withSleeper(sleeper);

		assertNotSame(policy, copy);
		assertEquals(75L, copy.getBackOffPeriod());
	}

	@Test
	public void setSleeperAcceptsSleeper() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		Sleeper sleeper = millis -> { };
		policy.setSleeper(sleeper);

		assertSame(policy, policy);
	}

	@Test
	public void backOffPeriodSupplierRejectsNull() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		try {
			policy.backOffPeriodSupplier(null);
			fail("Expected an exception");
		}
		catch (Exception expected) {
			// Assert.notNull rejects the null supplier.
		}
	}
}
