package com.github.robozonky.strategy.natural.conditions;

import com.github.robozonky.api.Ratio;
import com.github.robozonky.strategy.natural.Wrapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RangeCondition_ArrowTest {

    private static final Function<Wrapper<?>, Integer> VALUE = wrapper -> 4;

    private static Domain<Integer> integerDomain() {
        return new Domain<>(Integer.class, 0, 10);
    }

    @Test
    public void lessThanAcceptsValuesBelowThreshold() {
        RangeCondition<Integer> condition = RangeCondition.lessThan(VALUE, integerDomain(), 5);
        assertTrue(condition.test(null));
        assertFalse(RangeCondition.lessThan(wrapper -> 5, integerDomain(), 5).test(null));
        assertEquals("Range: (-inf.; 5)", condition.toString());
    }

    @Test
    public void moreThanAcceptsValuesAboveThreshold() {
        RangeCondition<Integer> condition = RangeCondition.moreThan(wrapper -> 6, integerDomain(), 5);
        assertTrue(condition.test(null));
        assertFalse(RangeCondition.moreThan(wrapper -> 5, integerDomain(), 5).test(null));
        assertEquals("Range: (5; +inf.)", condition.toString());
    }

    @Test
    public void exactIncludesBothEndpoints() {
        RangeCondition<Integer> condition = RangeCondition.exact(wrapper -> 3, integerDomain(), 3, 7);
        assertTrue(condition.test(null));
        assertTrue(RangeCondition.exact(wrapper -> 7, integerDomain(), 3, 7).test(null));
        assertFalse(RangeCondition.exact(wrapper -> 2, integerDomain(), 3, 7).test(null));
        assertEquals("Range: <3; 7>", condition.toString());
    }

    @Test
    public void lessThanRejectsThresholdOutsideDomain() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> RangeCondition.lessThan(VALUE, integerDomain(), 11));
    }

    @Test
    public void exactRejectsMinimumAboveMaximum() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> RangeCondition.exact(VALUE, integerDomain(), 8, 3));
    }

    @Test
    public void relativeLessThanUsesPartOverSum() {
        Function<Wrapper<?>, Integer> part = wrapper -> 4;
        Function<Wrapper<?>, Integer> sum = wrapper -> 10;
        Ratio threshold = Ratio.fromRaw(new BigDecimal("0.5"));
        RangeCondition<Ratio> condition = RangeCondition.relativeLessThan(part, sum, threshold);
        assertTrue(condition.test(null));
        assertFalse(RangeCondition.relativeLessThan(wrapper -> 5, sum, threshold).test(null));
        assertEquals("Relative range: (-inf.; " + threshold + ")", condition.toString());
    }

    @Test
    public void relativeMoreThanUsesPartOverSum() {
        Function<Wrapper<?>, Integer> sum = wrapper -> 10;
        RangeCondition<Ratio> condition = RangeCondition.relativeMoreThan(wrapper -> 6, sum,
                Ratio.fromRaw(new BigDecimal("0.5")));
        assertTrue(condition.test(null));
        assertFalse(RangeCondition.relativeMoreThan(wrapper -> 5, sum,
                Ratio.fromRaw(new BigDecimal("0.5"))).test(null));
    }

    @Test
    public void relativeExactIncludesBothEndpoints() {
        Function<Wrapper<?>, Integer> sum = wrapper -> 10;
        Ratio minimum = Ratio.fromRaw(new BigDecimal("0.3"));
        Ratio maximum = Ratio.fromRaw(new BigDecimal("0.7"));
        RangeCondition<Ratio> condition = RangeCondition.relativeExact(wrapper -> 3, sum, minimum, maximum);
        assertTrue(condition.test(null));
        assertTrue(RangeCondition.relativeExact(wrapper -> 7, sum, minimum, maximum).test(null));
        assertFalse(RangeCondition.relativeExact(wrapper -> 2, sum, minimum, maximum).test(null));
    }
}
