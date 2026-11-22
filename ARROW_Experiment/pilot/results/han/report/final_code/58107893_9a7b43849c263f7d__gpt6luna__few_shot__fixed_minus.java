package com.github.robozonky.strategy.natural.conditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.robozonky.api.Ratio;
import com.github.robozonky.strategy.natural.Wrapper;

public class RangeCondition_ArrowTest {

    private static final Function<Wrapper<?>, Integer> INTEGER_VALUE = wrapper -> 4;
    private static final Function<Wrapper<?>, Integer> PART = wrapper -> 4;
    private static final Function<Wrapper<?>, Integer> SUM = wrapper -> 10;

    @Test
    public void lessThanAcceptsOnlyValuesBelowThreshold() {
        RangeCondition<Integer> condition = RangeCondition.lessThan(INTEGER_VALUE, Domain.INTEGER, 5);
        assertTrue(condition.test(null));
        assertEquals("Range: (-inf.; 5)", condition.toString());
    }

    @Test
    public void moreThanAcceptsOnlyValuesAboveThreshold() {
        RangeCondition<Integer> condition = RangeCondition.moreThan(INTEGER_VALUE, Domain.INTEGER, 5);
        assertFalse(condition.test(null));
        assertEquals("Range: (5; +inf.)", condition.toString());
    }

    @Test
    public void exactIncludesBothEndpoints() {
        RangeCondition<Integer> condition = RangeCondition.exact(INTEGER_VALUE, Domain.INTEGER, 3, 7);
        assertTrue(condition.test(null));
        assertEquals("Range: <3; 7>", condition.toString());
    }

    @Test
    public void lessThanRejectsThresholdOutsideDomain() {
        try {
            RangeCondition.lessThan(INTEGER_VALUE, Domain.INTEGER, 11);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Threshold"));
        }
    }

    @Test
    public void exactRejectsMinimumGreaterThanMaximum() {
        try {
            RangeCondition.exact(INTEGER_VALUE, Domain.INTEGER, 8, 3);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("over maximum"));
        }
    }

    @Test
    public void relativeLessThanUsesPartOverSum() {
        Ratio threshold = Ratio.fromRaw(new BigDecimal("0.5"));
        RangeCondition<Ratio> condition = RangeCondition.relativeLessThan(PART, SUM, threshold);
        assertTrue(condition.test(null));
        assertEquals("Relative range: (-inf.; " + threshold + ")", condition.toString());
    }

    @Test
    public void relativeMoreThanUsesPartOverSum() {
        Ratio threshold = Ratio.fromRaw(new BigDecimal("0.5"));
        RangeCondition<Ratio> condition = RangeCondition.relativeMoreThan(PART, SUM, threshold);
        assertFalse(condition.test(null));
    }

    @Test
    public void relativeExactIncludesBothEndpoints() {
        Ratio minimum = Ratio.fromRaw(new BigDecimal("0.4"));
        Ratio maximum = Ratio.fromRaw(new BigDecimal("0.6"));
        RangeCondition<Ratio> condition = RangeCondition.relativeExact(PART, SUM, minimum, maximum);
        assertTrue(condition.test(null));
    }
}
