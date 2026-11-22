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

    private static Wrapper<?> wrapper() {
        return new Wrapper<Object>() {
            @Override
            public Object getOriginal() {
                return new Object();
            }

            @Override
            public BigDecimal interestRate() {
                return BigDecimal.ZERO;
            }

            @Override
            public BigDecimal purchasePrice() {
                return BigDecimal.ZERO;
            }

            @Override
            public BigDecimal salePrice() {
                return BigDecimal.ZERO;
            }

            @Override
            public Ratio saleFee() {
                return Ratio.ZERO;
            }
        };
    }

    private static Domain<Integer> domain() {
        return new Domain<Integer>(Integer.class, 0, 10);
    }

    @Test
    public void lessThanAcceptsValuesBelowThreshold() {
        RangeCondition<Integer> condition = RangeCondition.lessThan(VALUE, domain(), 5);
        assertTrue(condition.test(wrapper()));
        assertFalse(RangeCondition.lessThan((Wrapper<?> w) -> 5, domain(), 5).test(wrapper()));
        assertEquals("Range: (-inf.; 5)", condition.toString());
    }

    @Test
    public void moreThanAcceptsValuesAboveThreshold() {
        RangeCondition<Integer> condition = RangeCondition.moreThan((Wrapper<?> w) -> 6, domain(), 5);
        assertTrue(condition.test(wrapper()));
        assertFalse(RangeCondition.moreThan((Wrapper<?> w) -> 5, domain(), 5).test(wrapper()));
        assertEquals("Range: (5; +inf.)", condition.toString());
    }

    @Test
    public void exactIncludesBothEndpoints() {
        RangeCondition<Integer> condition = RangeCondition.exact((Wrapper<?> w) -> 3, domain(), 3, 7);
        assertTrue(condition.test(wrapper()));
        assertTrue(RangeCondition.exact((Wrapper<?> w) -> 7, domain(), 3, 7).test(wrapper()));
        assertFalse(RangeCondition.exact((Wrapper<?> w) -> 2, domain(), 3, 7).test(wrapper()));
        assertEquals("Range: <3; 7>", condition.toString());
    }

    @Test
    public void lessThanRejectsThresholdOutsideDomain() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> RangeCondition.lessThan(VALUE, domain(), 11));
    }

    @Test
    public void exactRejectsMinimumAboveMaximum() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> RangeCondition.exact(VALUE, domain(), 8, 3));
    }

    @Test
    public void relativeLessThanUsesPartOverSum() {
        Function<Wrapper<?>, Integer> part = wrapper -> 4;
        Function<Wrapper<?>, Integer> sum = wrapper -> 10;
        Ratio threshold = Ratio.fromRaw(new BigDecimal("0.5"));
        RangeCondition<Ratio> condition = RangeCondition.relativeLessThan(part, sum, threshold);
        assertTrue(condition.test(wrapper()));
        assertFalse(RangeCondition.relativeLessThan((Wrapper<?> w) -> 5, sum, threshold).test(wrapper()));
        assertEquals("Relative range: (-inf.; " + threshold + ")", condition.toString());
    }

    @Test
    public void relativeMoreThanUsesPartOverSum() {
        Function<Wrapper<?>, Integer> sum = wrapper -> 10;
        RangeCondition<Ratio> condition = RangeCondition.relativeMoreThan((Wrapper<?> w) -> 6, sum,
                Ratio.fromRaw(new BigDecimal("0.5")));
        assertTrue(condition.test(wrapper()));
        assertFalse(RangeCondition.relativeMoreThan((Wrapper<?> w) -> 5, sum,
                Ratio.fromRaw(new BigDecimal("0.5"))).test(wrapper()));
    }

    @Test
    public void relativeExactIncludesBothEndpoints() {
        Function<Wrapper<?>, Integer> sum = wrapper -> 10;
        Ratio minimum = Ratio.fromRaw(new BigDecimal("0.3"));
        Ratio maximum = Ratio.fromRaw(new BigDecimal("0.7"));
        RangeCondition<Ratio> condition = RangeCondition.relativeExact((Wrapper<?> w) -> 3, sum, minimum, maximum);
        assertTrue(condition.test(wrapper()));
        assertTrue(RangeCondition.relativeExact((Wrapper<?> w) -> 7, sum, minimum, maximum).test(wrapper()));
        assertFalse(RangeCondition.relativeExact((Wrapper<?> w) -> 2, sum, minimum, maximum).test(wrapper()));
    }
}
