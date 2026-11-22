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

    @Test
    public void lessThanAcceptsValuesBelowThreshold() {
        Wrapper<?> wrapper = null;
        Function<Wrapper<?>, Integer> value = ignored -> 5;
        RangeCondition<Integer> condition = RangeCondition.lessThan(value, new Domain<>(Integer.class, 0, 10), 6);
        assertTrue(condition.test(wrapper));
        assertFalse(RangeCondition.lessThan(value, new Domain<>(Integer.class, 0, 10), 5).test(wrapper));
        assertEquals("Range: (-inf.; 6)", condition.toString());
    }

    @Test
    public void moreThanAcceptsValuesAboveThreshold() {
        Wrapper<?> wrapper = null;
        Function<Wrapper<?>, Integer> value = ignored -> 5;
        RangeCondition<Integer> condition = RangeCondition.moreThan(value, new Domain<>(Integer.class, 0, 10), 4);
        assertTrue(condition.test(wrapper));
        assertFalse(RangeCondition.moreThan(value, new Domain<>(Integer.class, 0, 10), 5).test(wrapper));
        assertEquals("Range: (4; +inf.)", condition.toString());
    }

    @Test
    public void exactIncludesBothEndpoints() {
        Wrapper<?> wrapper = null;
        Function<Wrapper<?>, Integer> value = ignored -> 5;
        RangeCondition<Integer> condition = RangeCondition.exact(value, new Domain<>(Integer.class, 0, 10), 5, 7);
        assertTrue(condition.test(wrapper));
        assertFalse(RangeCondition.exact(value, new Domain<>(Integer.class, 0, 10), 6, 7).test(wrapper));
        assertEquals("Range: <5; 7>", condition.toString());
    }

    @Test
    public void lessThanRejectsThresholdOutsideDomain() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> RangeCondition.lessThan(ignored -> 5, new Domain<>(Integer.class, 0, 10), 11));
    }

    @Test
    public void exactRejectsMinimumAboveMaximum() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> RangeCondition.exact(ignored -> 5, new Domain<>(Integer.class, 0, 10), 8, 3));
    }

    @Test
    public void relativeLessThanUsesPartOverSum() {
        Wrapper<?> wrapper = null;
        Function<Wrapper<?>, Integer> part = ignored -> 4;
        Function<Wrapper<?>, Integer> sum = ignored -> 10;
        Ratio threshold = Ratio.fromRaw(new BigDecimal("0.5"));
        RangeCondition<Ratio> condition = RangeCondition.relativeLessThan(part, sum, threshold);
        assertTrue(condition.test(wrapper));
        assertFalse(RangeCondition.relativeLessThan(ignored -> 5, sum, threshold).test(wrapper));
        assertEquals("Relative range: (-inf.; " + threshold + ")", condition.toString());
    }

    @Test
    public void relativeMoreThanUsesPartOverSum() {
        Wrapper<?> wrapper = null;
        Function<Wrapper<?>, Integer> part = ignored -> 6;
        Function<Wrapper<?>, Integer> sum = ignored -> 10;
        Ratio threshold = Ratio.fromRaw(new BigDecimal("0.5"));
        RangeCondition<Ratio> condition = RangeCondition.relativeMoreThan(part, sum, threshold);
        assertTrue(condition.test(wrapper));
        assertFalse(RangeCondition.relativeMoreThan(ignored -> 5, sum, threshold).test(wrapper));
    }

    @Test
    public void relativeExactIncludesBothEndpoints() {
        Wrapper<?> wrapper = null;
        Function<Wrapper<?>, Integer> part = ignored -> 5;
        Function<Wrapper<?>, Integer> sum = ignored -> 10;
        Ratio minimum = Ratio.fromRaw(new BigDecimal("0.5"));
        Ratio maximum = Ratio.fromRaw(new BigDecimal("0.7"));
        RangeCondition<Ratio> condition = RangeCondition.relativeExact(part, sum, minimum, maximum);
        assertTrue(condition.test(wrapper));
        assertFalse(RangeCondition.relativeExact(ignored -> 4, sum, minimum, maximum).test(wrapper));
    }
}
