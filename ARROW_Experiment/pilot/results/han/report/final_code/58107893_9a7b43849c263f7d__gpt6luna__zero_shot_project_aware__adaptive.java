package com.github.robozonky.strategy.natural.conditions;

import com.github.robozonky.api.Ratio;
import com.github.robozonky.strategy.natural.Wrapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RangeCondition_ArrowTest {

    private Domain<Integer> integerDomain() {
        return new Domain<>(Integer.class, 0, 10);
    }

    @Test
    public void lessThanAcceptsOnlyValuesBelowThreshold() {
        RangeCondition<Integer> condition = RangeCondition.lessThan(w -> 4, integerDomain(), 5);

        assertThat(condition.test(null)).isTrue();
        assertThat(condition.toString()).isEqualTo("Range: (-inf.; 5)");
    }

    @Test
    public void lessThanRejectsThresholdAndGreaterValues() {
        RangeCondition<Integer> atThreshold = RangeCondition.lessThan(w -> 5, integerDomain(), 5);
        RangeCondition<Integer> aboveThreshold = RangeCondition.lessThan(w -> 6, integerDomain(), 5);

        assertThat(atThreshold.test(null)).isFalse();
        assertThat(aboveThreshold.test(null)).isFalse();
    }

    @Test
    public void moreThanAcceptsOnlyValuesAboveThreshold() {
        RangeCondition<Integer> condition = RangeCondition.moreThan(w -> 6, integerDomain(), 5);

        assertThat(condition.test(null)).isTrue();
        assertThat(condition.toString()).isEqualTo("Range: (5; +inf.)");
    }

    @Test
    public void moreThanRejectsThresholdAndLowerValues() {
        RangeCondition<Integer> atThreshold = RangeCondition.moreThan(w -> 5, integerDomain(), 5);
        RangeCondition<Integer> belowThreshold = RangeCondition.moreThan(w -> 4, integerDomain(), 5);

        assertThat(atThreshold.test(null)).isFalse();
        assertThat(belowThreshold.test(null)).isFalse();
    }

    @Test
    public void exactIncludesBothEndpoints() {
        RangeCondition<Integer> condition = RangeCondition.exact(w -> 5, integerDomain(), 5, 7);

        assertThat(condition.test(null)).isTrue();
        assertThat(condition.toString()).isEqualTo("Range: <5; 7>");
    }

    @Test
    public void exactRejectsValuesOutsideRange() {
        RangeCondition<Integer> below = RangeCondition.exact(w -> 4, integerDomain(), 5, 7);
        RangeCondition<Integer> above = RangeCondition.exact(w -> 8, integerDomain(), 5, 7);

        assertThat(below.test(null)).isFalse();
        assertThat(above.test(null)).isFalse();
    }

    @Test
    public void lessThanRejectsThresholdOutsideDomain() {
        assertThatThrownBy(() -> RangeCondition.lessThan(w -> 1, integerDomain(), 11))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void moreThanRejectsThresholdOutsideDomain() {
        assertThatThrownBy(() -> RangeCondition.moreThan(w -> 1, integerDomain(), -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void exactRejectsMinimumOutsideDomain() {
        assertThatThrownBy(() -> RangeCondition.exact(w -> 1, integerDomain(), -1, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void exactRejectsMaximumOutsideDomain() {
        assertThatThrownBy(() -> RangeCondition.exact(w -> 1, integerDomain(), 1, 11))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void exactRejectsMinimumGreaterThanMaximum() {
        assertThatThrownBy(() -> RangeCondition.exact(w -> 1, integerDomain(), 7, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void relativeLessThanAcceptsSmallerRatio() {
        Function<Wrapper<?>, Integer> part = w -> 1;
        Function<Wrapper<?>, Integer> sum = w -> 4;
        Ratio threshold = Ratio.fromRaw(new BigDecimal("0.5"));
        RangeCondition<Ratio> condition = RangeCondition.relativeLessThan(part, sum, threshold);

        assertThat(condition.test(null)).isTrue();
        assertThat(condition.toString()).isEqualTo("Relative range: (-inf.; " + threshold + ")");
    }

    @Test
    public void relativeMoreThanAcceptsLargerRatio() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 4;
        Ratio threshold = Ratio.fromRaw(new BigDecimal("0.5"));
        RangeCondition<Ratio> condition = RangeCondition.relativeMoreThan(part, sum, threshold);

        assertThat(condition.test(null)).isTrue();
        assertThat(condition.toString()).isEqualTo("Relative range: (" + threshold + "; +inf.)");
    }

    @Test
    public void relativeExactIncludesEndpoints() {
        Function<Wrapper<?>, Integer> part = w -> 1;
        Function<Wrapper<?>, Integer> sum = w -> 4;
        Ratio minimum = Ratio.fromRaw(new BigDecimal("0.25"));
        Ratio maximum = Ratio.fromRaw(new BigDecimal("0.5"));
        RangeCondition<Ratio> condition = RangeCondition.relativeExact(part, sum, minimum, maximum);

        assertThat(condition.test(null)).isTrue();
        assertThat(condition.toString()).isEqualTo("Relative range: <" + minimum + "; " + maximum + ">");
    }
}
