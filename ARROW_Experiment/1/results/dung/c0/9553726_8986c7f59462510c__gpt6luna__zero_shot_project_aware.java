package org.javamoney.calc.common;

import javax.money.Monetary;
import javax.money.MonetaryAmount;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PresentValue_ArrowTest {

    @Test
    public void calculateDiscountsAmount() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(
                Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(0.10).create(),
                1);
        MonetaryAmount amount = Monetary.getDefaultAmountFactory()
                .setCurrency("USD").setNumber(110).create();

        MonetaryAmount result = PresentValue.calculate(amount, rateAndPeriods);

        assertEquals(Monetary.getDefaultAmountFactory()
                .setCurrency("USD").setNumber(100).create(), result);
    }

    @Test
    public void ofCreatesOperator() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(
                Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(0.05).create(),
                2);

        assertNotNull(PresentValue.of(rateAndPeriods));
    }

    @Test
    public void applyDiscountsAmount() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(
                Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(0.10).create(),
                1);
        PresentValue presentValue = PresentValue.of(rateAndPeriods);
        MonetaryAmount amount = Monetary.getDefaultAmountFactory()
                .setCurrency("USD").setNumber(110).create();

        MonetaryAmount result = presentValue.apply(amount);

        assertEquals(Monetary.getDefaultAmountFactory()
                .setCurrency("USD").setNumber(100).create(), result);
    }

    @Test
    public void toStringIncludesRateAndPeriods() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(
                Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(0.10).create(),
                1);

        String result = PresentValue.of(rateAndPeriods).toString();

        assertEquals("PresentValue{\n " + rateAndPeriods + "}", result);
    }

    @Test(expected = NullPointerException.class)
    public void calculateRejectsNullAmount() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(
                Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(0.10).create(),
                1);

        PresentValue.calculate(null, rateAndPeriods);
    }

    @Test(expected = NullPointerException.class)
    public void calculateRejectsNullRateAndPeriods() {
        MonetaryAmount amount = Monetary.getDefaultAmountFactory()
                .setCurrency("USD").setNumber(110).create();

        PresentValue.calculate(amount, null);
    }
}
