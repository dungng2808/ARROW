package org.javamoney.calc.common;

import javax.money.MonetaryAmount;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PresentValue_ArrowTest {

    @Test
    public void ofReturnsOperator() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(0.0, 1);
        PresentValue presentValue = PresentValue.of(rateAndPeriods);

        assertNotNull(presentValue);
        assertTrue(presentValue.toString().contains(rateAndPeriods.toString()));
    }

    @Test
    public void calculateRejectsNullAmount() {
        try {
            PresentValue.calculate(null, RateAndPeriods.of(0.0, 1));
        } catch (NullPointerException expected) {
            return;
        }
        throw new AssertionError("Expected an exception");
    }

    @Test
    public void calculateRejectsNullRateAndPeriods() {
        try {
            PresentValue.calculate(null, null);
        } catch (NullPointerException expected) {
            return;
        }
        throw new AssertionError("Expected an exception");
    }

    @Test
    public void applyReturnsSameAmountAtZeroRate() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(0.0, 1);
        PresentValue presentValue = PresentValue.of(rateAndPeriods);
        MonetaryAmount amount = null;

        try {
            presentValue.apply(amount);
        } catch (NullPointerException expected) {
            return;
        }
        throw new AssertionError("Expected an exception");
    }
}
