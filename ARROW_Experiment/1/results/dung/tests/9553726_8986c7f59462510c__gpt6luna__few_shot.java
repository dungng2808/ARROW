package org.javamoney.calc.common;

import javax.money.MonetaryAmount;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class PresentValue_ArrowTest {

    @Test
    public void calculateDiscountsFutureAmount() {
        RateAndPeriods rateAndPeriods = new RateAndPeriods(0.1, 1);
        MonetaryAmount amount = org.javamoney.moneta.Money.of(110, "USD");

        MonetaryAmount result = PresentValue.calculate(amount, rateAndPeriods);

        assertEquals(org.javamoney.moneta.Money.of(100, "USD"), result);
    }

    @Test
    public void ofCreatesOperatorThatAppliesPresentValueCalculation() {
        RateAndPeriods rateAndPeriods = new RateAndPeriods(0.1, 1);
        MonetaryAmount amount = org.javamoney.moneta.Money.of(110, "USD");

        MonetaryAmount result = PresentValue.of(rateAndPeriods).apply(amount);

        assertEquals(org.javamoney.moneta.Money.of(100, "USD"), result);
    }

    @Test
    public void calculateRejectsNullAmount() {
        RateAndPeriods rateAndPeriods = new RateAndPeriods(0.1, 1);

        assertThrows(NullPointerException.class, () -> PresentValue.calculate(null, rateAndPeriods));
    }

    @Test
    public void calculateRejectsNullRateAndPeriods() {
        MonetaryAmount amount = org.javamoney.moneta.Money.of(110, "USD");

        assertThrows(NullPointerException.class, () -> PresentValue.calculate(amount, null));
    }

    @Test
    public void toStringIncludesRateAndPeriods() {
        RateAndPeriods rateAndPeriods = new RateAndPeriods(0.1, 1);

        assertNotNull(PresentValue.of(rateAndPeriods).toString());
    }
}
