package org.springframework.retry.backoff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;

public class FixedBackOffPolicy_ArrowTest {

	@Test
	public void defaultBackOffPeriodIsOneSecond() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();

		assertEquals(1000L, policy.getBackOffPeriod());
		assertEquals("FixedBackOffPolicy[backOffPeriod=1000]", policy.toString());
	}

	@Test
	public void configuredBackOffPeriodIsReturned() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(250L);

		assertEquals(250L, policy.getBackOffPeriod());
		assertEquals("FixedBackOffPolicy[backOffPeriod=250]", policy.toString());
	}

	@Test
	public void nonPositiveBackOffPeriodIsClampedToOne() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(0L);

		assertEquals(1L, policy.getBackOffPeriod());
	}

	@Test
	public void supplierProvidesBackOffPeriod() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		AtomicLong period = new AtomicLong(12L);
		policy.backOffPeriodSupplier(period::get);

		assertEquals(12L, policy.getBackOffPeriod());
		period.set(34L);
		assertEquals(34L, policy.getBackOffPeriod());
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullSupplierIsRejected() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.backOffPeriodSupplier(null);
	}

	@Test
	public void withSleeperCopiesPeriodAndUsesProvidedSleeper() throws Exception {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(25L);
		final long[] slept = {-1L};
		Sleeper sleeper = period -> slept[0] = period;

		FixedBackOffPolicy copy = policy.withSleeper(sleeper);
		assertNotNull(copy);
		assertEquals(25L, copy.getBackOffPeriod());
		copy.backOff();

		assertEquals(25L, slept[0]);
	}

	@Test
	public void interruptedSleepRestoresInterruptAndThrowsBackOffInterruptedException() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setSleeper(period -> {
			throw new InterruptedException("interrupted");
		});

		try {
			policy.backOff();
			fail("Expected BackOffInterruptedException");
		}
		catch (BackOffInterruptedException expected) {
			assertSame(InterruptedException.class, expected.getCause().getClass());
			assertEquals(true, Thread.currentThread().isInterrupted());
		}
		finally {
			Thread.interrupted();
		}
	}
}
