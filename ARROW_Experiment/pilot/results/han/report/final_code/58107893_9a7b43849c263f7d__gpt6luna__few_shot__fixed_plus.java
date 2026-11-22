package com.github.robozonky.strategy.natural.conditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.robozonky.strategy.natural.Wrapper;

public class RangeCondition_ArrowTest {

    private static final Function<Wrapper<?>, Integer> INTEGER_VALUE =
            wrapper -> (Integer) wrapper.getOriginal();
    private static final Function<Wrapper<?>, Integer> PART =
            wrapper -> (Integer) wrapper.getOriginal();
    private static final Function<Wrapper<?>, Integer> SUM = wrapper -> 10;

    private static Wrapper<?> wrapper(final int value) {
        return new Wrapper<Integer>() {
            @Override
            public Integer getOriginal() {
                return value;
            }

            @Override
            public java.math.BigDecimal saleFee() {
                return java.math.BigDecimal.ZERO;
            }
        };
    }

    private static Domain<Integer> domain() {
        return new Domain<>(Integer.class, 0, 10);
    }

    @Test
    public void lessThanAcceptsOnlyValuesBelowThreshold() {
        RangeCondition<Integer> condition = RangeCondition.lessThan(INTEGER_VALUE, domain(), 5);
        assertTrue(condition.test(wrapper(4)));
        assertFalse(condition.test(wrapper(5)));
        assertEquals("Range: (-inf.; 5)", condition.toString());
    }

    @Test
    public void moreThanAcceptsOnlyValuesAboveThreshold() {
        RangeCondition<Integer> condition = RangeCondition.moreThan(INTEGER_VALUE, domain(), 5);
        assertTrue(condition.test(wrapper(6)));
        assertFalse(condition.test(wrapper(5)));
        assertEquals("Range: (5; +inf.)", condition.toString());
    }

    @Test
    public void exactIncludesBothEndpoints() {
        RangeCondition<Integer> condition = RangeCondition.exact(INTEGER_VALUE, domain(), 3, 7);
        assertTrue(condition.test(wrapper(3)));
        assertTrue(condition.test(wrapper(7)));
        assertFalse(condition.test(wrapper(2)));
        assertEquals("Range: <3; 7>", condition.toString());
    }

    @Test
    public void lessThanRejectsThresholdOutsideDomain() {
        try {
            RangeCondition.lessThan(INTEGER_VALUE, domain(), 11);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Threshold"));
        }
    }

    @Test
    public void exactRejectsMinimumGreaterThanMaximum() {
        try {
            RangeCondition.exact(INTEGER_VALUE, domain(), 8, 3);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("over maximum"));
        }
    }

    @Test
    public void relativeLessThanUsesPartOverSum() {
        com.github.robozonky.api.Ratio threshold =
                com.github.robozonky.api.Ratio.fromRaw(new java.math.BigDecimal("0.5"));
        RangeCondition<com.github.robozonky.api.Ratio> condition =
                RangeCondition.relativeLessThan(PART, SUM, threshold);
        assertTrue(condition.test(wrapper(4)));
        assertFalse(condition.test(wrapper(5)));
        assertEquals("Relative range: (-inf.; " + threshold + ")", condition.toString());
    }

    @Test
    public void relativeMoreThanUsesPartOverSum() {
        com.github.robozonky.api.Ratio threshold =
                com.github.robozonky.api.Ratio.fromRaw(new java.math.BigDecimal("0.5"));
        RangeCondition<com.github.robozonky.api.Ratio> condition =
                RangeCondition.relativeMoreThan(PART, SUM, threshold);
        assertTrue(condition.test(wrapper(6)));
        assertFalse(condition.test(wrapper(5)));
    }

    @Test
    public void relativeExactIncludesBothEndpoints() {
        com.github.robozonky.api.Ratio minimum =
                com.github.robozonky.api.Ratio.fromRaw(new java.math.BigDecimal("0.4"));
        com.github.robozonky.api.Ratio maximum =
                com.github.robozonky.api.Ratio.fromRaw(new java.math.BigDecimal("0.6"));
        RangeCondition<com.github.robozonky.api.Ratio> condition =
                RangeCondition.relativeExact(PART, SUM, minimum, maximum);
        assertTrue(condition.test(wrapper(4)));
        assertTrue(condition.test(wrapper(6)));
        assertFalse(condition.test(wrapper(3)));
    }
}
