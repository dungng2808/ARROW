package org.javamoney.calc.common;

import javax.money.MonetaryAmount;
import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;

public class PresentValue_ArrowTest {

    @Test
    public void testOf() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(BigDecimal.valueOf(0.05), 1);
        PresentValue presentValue = PresentValue.of(rateAndPeriods);
        assertNotNull(presentValue);
    }

    @Test
    public void testCalculate() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(BigDecimal.valueOf(0.05), 1);
        MonetaryAmount amount = MonetaryAmount.class.cast(BigDecimal.valueOf(100));
        MonetaryAmount result = PresentValue.calculate(amount, rateAndPeriods);
        assertNotNull(result);
    }

    @Test
    public void testApply() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(BigDecimal.valueOf(0.05), 1);
        PresentValue presentValue = PresentValue.of(rateAndPeriods);
        MonetaryAmount amount = MonetaryAmount.class.cast(BigDecimal.valueOf(100));
        MonetaryAmount result = presentValue.apply(amount);
        assertNotNull(result);
    }

    @Test
    public void testToString() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(BigDecimal.valueOf(0.05), 1);
        PresentValue presentValue = PresentValue.of(rateAndPeriods);
        String result = presentValue.toString();
        assertNotNull(result);
        assertTrue(result.contains("PresentValue"));
    }

    @Test(expected = NullPointerException.class)
    public void testCalculateWithNullAmount() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(BigDecimal.valueOf(0.05), 1);
        PresentValue.calculate(null, rateAndPeriods);
    }

    @Test(expected = NullPointerException.class)
    public void testCalculateWithNullRateAndPeriods() {
        MonetaryAmount amount = MonetaryAmount.class.cast(BigDecimal.valueOf(100));
        PresentValue.calculate(amount, null);
    }

    @Test(expected = NullPointerException.class)
    public void testApplyWithNullAmount() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(BigDecimal.valueOf(0.05), 1);
        PresentValue presentValue = PresentValue.of(rateAndPeriods);
        presentValue.apply(null);
    }
}
