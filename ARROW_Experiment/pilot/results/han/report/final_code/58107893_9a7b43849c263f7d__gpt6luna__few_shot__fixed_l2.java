package com.github.robozonky.strategy.natural.conditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.github.robozonky.api.Ratio;
import com.github.robozonky.strategy.natural.Wrapper;

public class RangeCondition_ArrowTest {

    private static final Function<Wrapper<?>, Integer> INTEGER_VALUE =
            wrapper -> (Integer) wrapper.getOriginal();
    private static final Function<Wrapper<?>, Integer> PART =
            wrapper -> (Integer) wrapper.getOriginal();
    private static final Function<Wrapper<?>, Integer> SUM = wrapper -> 10;

    private static Wrapper<?> wrapperWithAmount(final int amount) {
        return new Wrapper<Integer>() {
            @Override
            public Integer getOriginal() {
                return amount;
            }
        };
    }

    @Test
    public void lessThanAcceptsOnlyValuesBelowThreshold() {
        RangeCondition<Integer> condition =
                RangeCondition.lessThan(INTEGER_VALUE, new Domain<>(Integer.class, 0, 10), 5);
        assertTrue(condition.test(wrapperWithAmount(4)));
        assertFalse(condition.test(wrapperWithAmount(5)));
        assertEquals("Range: (-inf.; 5)", condition.toString());
    }

    @Test
    public void moreThanAcceptsOnlyValuesAboveThreshold() {
        RangeCondition<Integer> condition =
                RangeCondition.moreThan(INTEGER_VALUE, new Domain<>(Integer.class, 0, 10), 5);
        assertTrue(condition.test(wrapperWithAmount(6)));
        assertFalse(condition.test(wrapperWithAmount(5)));
        assertEquals("Range: (5; +inf.)", condition.toString());
    }

    @Test
    public void exactIncludesBothEndpoints() {
        RangeCondition<Integer> condition =
                RangeCondition.exact(INTEGER_VALUE, new Domain<>(Integer.class, 0, 10), 3, 7);
        assertTrue(condition.test(wrapperWithAmount(3)));
        assertTrue(condition.test(wrapperWithAmount(7)));
        assertFalse(condition.test(wrapperWithAmount(2)));
        assertEquals("Range: <3; 7>", condition.toString());
    }

    @Test
    public void lessThanRejectsThresholdOutsideDomain() {
        try {
            RangeCondition.lessThan(INTEGER_VALUE, new Domain<>(Integer.class, 0, 10), 11);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Threshold"));
        }
    }

    @Test
    public void exactRejectsMinimumGreaterThanMaximum() {
        try {
            RangeCondition.exact(INTEGER_VALUE, new Domain<>(Integer.class, 0, 10), 8, 3);
            fail("Expected an IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("over maximum"));
        }
    }

    @Test
    public void relativeLessThanUsesPartOverSum() {
        Ratio threshold = Ratio.fromRaw(new java.math.BigDecimal("0.5"));
        RangeCondition<Ratio> condition =
                RangeCondition.relativeLessThan(PART, SUM, threshold);
        assertTrue(condition.test(wrapperWithAmount(4)));
        assertFalse(condition.test(wrapperWithAmount(5)));
        assertEquals("Relative range: (-inf.; " + threshold + ")", condition.toString());
    }

    @Test
    public void relativeMoreThanUsesPartOverSum() {
        Ratio threshold = Ratio.fromRaw(new java.math.BigDecimal("0.5"));
        RangeCondition<Ratio> condition =
                RangeCondition.relativeMoreThan(PART, SUM, threshold);
        assertTrue(condition.test(wrapperWithAmount(6)));
        assertFalse(condition.test(wrapperWithAmount(5)));
    }

    @Test
    public void relativeExactIncludesBothEndpoints() {
        Ratio minimum = Ratio.fromRaw(new java.math.BigDecimal("0.4"));
        Ratio maximum = Ratio.fromRaw(new java.math.BigDecimal("0.6"));
        RangeCondition<Ratio> condition =
                RangeCondition.relativeExact(PART, SUM, minimum, maximum);
        assertTrue(condition.test(wrapperWithAmount(4)));
        assertTrue(condition.test(wrapperWithAmount(6)));
        assertFalse(condition.test(wrapperWithAmount(3)));
    }
}
