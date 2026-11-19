package org.springframework.retry.backoff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.backoff.Sleeper;

public class FixedBackOffPolicy_ArrowTest {

	@Test
	public void testDefaultBackOffPeriod() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		assertThat(policy.getBackOffPeriod()).isEqualTo(1000L);
	}

	@Test
	public void testSetBackOffPeriod() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(5000L);
		assertThat(policy.getBackOffPeriod()).isEqualTo(5000L);
	}

	@Test
	public void testSetBackOffPeriodZero() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(0L);
		assertThat(policy.getBackOffPeriod()).isEqualTo(1L);
	}

	@Test
	public void testSetBackOffPeriodNegative() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(-100L);
		assertThat(policy.getBackOffPeriod()).isEqualTo(1L);
	}

	@Test
	public void testBackOffPeriodSupplier() {
		AtomicLong counter = new AtomicLong(0);
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.backOffPeriodSupplier(() -> counter.incrementAndGet() * 1000L);
		assertThat(policy.getBackOffPeriod()).isEqualTo(1000L);
		assertThat(policy.getBackOffPeriod()).isEqualTo(2000L);
	}

	@Test
	public void testSetSleeper() {
		Sleeper mockSleeper = mock(Sleeper.class);
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setSleeper(mockSleeper);
		policy.setBackOffPeriod(1000L);
		assertThat(policy.getBackOffPeriod()).isEqualTo(1000L);
	}

	@Test
	public void testDoBackOff() throws Exception {
		Sleeper mockSleeper = mock(Sleeper.class);
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setSleeper(mockSleeper);
		policy.setBackOffPeriod(1000L);
		policy.doBackOff();
		verify(mockSleeper).sleep(1000L);
	}

	@Test
	public void testToString() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(2000L);
		String result = policy.toString();
		assertThat(result).contains("FixedBackOffPolicy[backOffPeriod=2000]");
	}

	@Test
	public void testWithSleeper() {
		Sleeper mockSleeper = mock(Sleeper.class);
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		policy.setBackOffPeriod(1500L);
		FixedBackOffPolicy newPolicy = policy.withSleeper(mockSleeper);
		assertThat(newPolicy.getBackOffPeriod()).isEqualTo(1500L);
	}

	@Test
	public void testBackOffPeriodSupplierNull() {
		FixedBackOffPolicy policy = new FixedBackOffPolicy();
		assertThatThrownBy(() -> policy.backOffPeriodSupplier(null))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
