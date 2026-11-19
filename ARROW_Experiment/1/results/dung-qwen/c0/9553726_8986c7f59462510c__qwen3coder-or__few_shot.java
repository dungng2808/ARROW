package org.javamoney.calc.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.money.MonetaryAmount;

import org.junit.Test;

public class PresentValue_ArrowTest {

    @Test
    public void testOf() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(0.05, 10);
        PresentValue presentValue = PresentValue.of(rateAndPeriods);
        assertNotNull(presentValue);
    }

    @Test
    public void testCalculate() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(0.05, 10);
        MonetaryAmount amount = mock(MonetaryAmount.class);
        when(amount.divide(PresentValueFactor.calculate(rateAndPeriods))).thenReturn(amount);
        
        MonetaryAmount result = PresentValue.calculate(amount, rateAndPeriods);
        assertEquals(amount, result);
    }

    @Test
    public void testApply() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(0.05, 10);
        PresentValue presentValue = PresentValue.of(rateAndPeriods);
        MonetaryAmount amount = mock(MonetaryAmount.class);
        when(amount.divide(PresentValueFactor.calculate(rateAndPeriods))).thenReturn(amount);
        
        MonetaryAmount result = presentValue.apply(amount);
        assertEquals(amount, result);
    }

    @Test
    public void testToString() {
        RateAndPeriods rateAndPeriods = RateAndPeriods.of(0.05, 10);
        PresentValue presentValue = PresentValue.of(rateAndPeriods);
        String result = presentValue.toString();
        assertNotNull(result);
        assertEquals("PresentValue{" +
                "\n " + rateAndPeriods +
                '}', result);
    }
}
