package org.springframework.retry.backoff;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FixedBackOffPolicy_ArrowTest {

	@Test
	public void defaultBackOffPeriodIsOneSecond() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();

		assertEquals(1000L, policy.getBackOffPeriod());
		assertEquals("FixedBackOffPolicy[backOffPeriod=1000]", policy.toString());
	}

	@Test
	public void setBackOffPeriodUsesConfiguredPositiveValue() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(250L);

		assertEquals(250L, policy.getBackOffPeriod());
		assertEquals("FixedBackOffPolicy[backOffPeriod=250]", policy.toString());
	}

	@Test
	public void setBackOffPeriodClampsZeroToOne() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(0L);

		assertEquals(1L, policy.getBackOffPeriod());
	}

	@Test
	public void setBackOffPeriodClampsNegativeValueToOne() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(-10L);

		assertEquals(1L, policy.getBackOffPeriod());
	}

	@Test
	public void supplierProvidesBackOffPeriod() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		AtomicLong supplied = new AtomicLong(42L);
		Supplier<Long> supplier = supplied::get;
		policy.backOffPeriodSupplier(supplier);

		assertEquals(42L, policy.getBackOffPeriod());
		supplied.set(84L);
		assertEquals(84L, policy.getBackOffPeriod());
	}

	@Test
	public void nullSupplierIsRejected() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();

		assertThrows(IllegalArgumentException.class, () -> policy.backOffPeriodSupplier(null));
	}

	@Test
	public void withSleeperReturnsSeparatePolicyWithSamePeriod() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(75L);

		FixedBackOffPolicy copy = policy.withSleeper(milliseconds -> { });

		assertNotSame(policy, copy);
		assertEquals(75L, copy.getBackOffPeriod());
		assertEquals("FixedBackOffPolicy[backOffPeriod=75]", copy.toString());
	}
}
