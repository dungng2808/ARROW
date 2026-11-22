package com.github.robozonky.strategy.natural.conditions;

import com.github.robozonky.api.Ratio;
import com.github.robozonky.strategy.natural.Wrapper;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RangeCondition_ArrowTest {

    private static final Function<Wrapper<?>, Integer> VALUE = wrapper -> (Integer) wrapper.getValue();

    @Test
    public void lessThanAcceptsValuesBelowThreshold() {
        RangeCondition<Integer> condition = RangeCondition.lessThan(VALUE, Domain.of(0, 10), 5);
        assertTrue(condition.test(Wrapper.of(4)));
        assertFalse(condition.test(Wrapper.of(5)));
        assertEquals("Range: (-inf.; 5)", condition.toString());
    }

    @Test
    public void moreThanAcceptsValuesAboveThreshold() {
        RangeCondition<Integer> condition = RangeCondition.moreThan(VALUE, Domain.of(0, 10), 5);
        assertTrue(condition.test(Wrapper.of(6)));
        assertFalse(condition.test(Wrapper.of(5)));
        assertEquals("Range: (5; +inf.)", condition.toString());
    }

    @Test
    public void exactIncludesBothEndpoints() {
        RangeCondition<Integer> condition = RangeCondition.exact(VALUE, Domain.of(0, 10), 3, 7);
        assertTrue(condition.test(Wrapper.of(3)));
        assertTrue(condition.test(Wrapper.of(7)));
        assertFalse(condition.test(Wrapper.of(2)));
        assertEquals("Range: <3; 7>", condition.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void lessThanRejectsThresholdOutsideDomain() {
        RangeCondition.lessThan(VALUE, Domain.of(0, 10), 11);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exactRejectsMinimumAboveMaximum() {
        RangeCondition.exact(VALUE, Domain.of(0, 10), 8, 3);
    }

    @Test
    public void relativeLessThanUsesPartOverSum() {
        Function<Wrapper<?>, Integer> part = wrapper -> (Integer) wrapper.getValue();
        Function<Wrapper<?>, Integer> sum = wrapper -> 10;
        RangeCondition<Ratio> condition = RangeCondition.relativeLessThan(part, sum, Ratio.fromRaw(new BigDecimal("0.5")));
        assertTrue(condition.test(Wrapper.of(4)));
        assertFalse(condition.test(Wrapper.of(5)));
        assertEquals("Relative range: (-inf.; " + Ratio.fromRaw(new BigDecimal("0.5")) + ")", condition.toString());
    }

    @Test
    public void relativeMoreThanUsesPartOverSum() {
        Function<Wrapper<?>, Integer> part = wrapper -> (Integer) wrapper.getValue();
        Function<Wrapper<?>, Integer> sum = wrapper -> 10;
        RangeCondition<Ratio> condition = RangeCondition.relativeMoreThan(part, sum, Ratio.fromRaw(new BigDecimal("0.5")));
        assertTrue(condition.test(Wrapper.of(6)));
        assertFalse(condition.test(Wrapper.of(5)));
    }

    @Test
    public void relativeExactIncludesBothEndpoints() {
        Function<Wrapper<?>, Integer> part = wrapper -> (Integer) wrapper.getValue();
        Function<Wrapper<?>, Integer> sum = wrapper -> 10;
        Ratio minimum = Ratio.fromRaw(new BigDecimal("0.3"));
        Ratio maximum = Ratio.fromRaw(new BigDecimal("0.7"));
        RangeCondition<Ratio> condition = RangeCondition.relativeExact(part, sum, minimum, maximum);
        assertTrue(condition.test(Wrapper.of(3)));
        assertTrue(condition.test(Wrapper.of(7)));
        assertFalse(condition.test(Wrapper.of(2)));
    }
}
