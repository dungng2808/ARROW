package com.github.robozonky.strategy.natural.conditions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.github.robozonky.api.Ratio;
import com.github.robozonky.strategy.natural.Wrapper;

public class RangeCondition_ArrowTest {

    @Test
    public void testLessThan() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> domain = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        RangeCondition<Integer> condition = RangeCondition.lessThan(accessor, domain, 10);
        assertNotNull(condition);
        assertEquals("Range: (-inf.; 10)", condition.toString());
    }

    @Test
    public void testMoreThan() {
        Function<Wrapper<?>, Integer> accessor = w -> 15;
        Domain<Integer> domain = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        RangeCondition<Integer> condition = RangeCondition.moreThan(accessor, domain, 10);
        assertNotNull(condition);
        assertEquals("Range: (10; +inf.)", condition.toString());
    }

    @Test
    public void testExact() {
        Function<Wrapper<?>, Integer> accessor = w -> 7;
        Domain<Integer> domain = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        RangeCondition<Integer> condition = RangeCondition.exact(accessor, domain, 5, 10);
        assertNotNull(condition);
        assertEquals("Range: <5; 10>", condition.toString());
    }

    @Test
    public void testRelativeLessThan() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.fromRaw(BigDecimal.valueOf(0.5));
        RangeCondition<Ratio> condition = RangeCondition.relativeLessThan(part, sum, threshold);
        assertNotNull(condition);
        assertEquals("Relative range: (-inf.; 0.5)", condition.toString());
    }

    @Test
    public void testRelativeMoreThan() {
        Function<Wrapper<?>, Integer> part = w -> 7;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.fromRaw(BigDecimal.valueOf(0.5));
        RangeCondition<Ratio> condition = RangeCondition.relativeMoreThan(part, sum, threshold);
        assertNotNull(condition);
        assertEquals("Relative range: (0.5; +inf.)", condition.toString());
    }

    @Test
    public void testRelativeExact() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio minimum = Ratio.fromRaw(BigDecimal.valueOf(0.3));
        Ratio maximum = Ratio.fromRaw(BigDecimal.valueOf(0.7));
        RangeCondition<Ratio> condition = RangeCondition.relativeExact(part, sum, minimum, maximum);
        assertNotNull(condition);
        assertEquals("Relative range: <0.3; 0.7>", condition.toString());
    }

    @Test
    public void testTest() {
        Function<Wrapper<?>, Integer> accessor = w -> 7;
        Predicate<Integer> acceptor = v -> v > 5;
        RangeCondition<Integer> condition = new RangeCondition<>(accessor, acceptor, "test");
        Wrapper<?> wrapper = mock(Wrapper.class);
        assertTrue(condition.test(wrapper));
    }

    @Test
    public void testToString() {
        Function<Wrapper<?>, Integer> accessor = w -> 7;
        Predicate<Integer> acceptor = v -> v > 5;
        RangeCondition<Integer> condition = new RangeCondition<>(accessor, acceptor, "test");
        assertEquals("test", condition.toString());
    }

    @Test
    public void testLessThanInvalidThreshold() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> domain = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.lessThan(accessor, domain, -10));
    }

    @Test
    public void testMoreThanInvalidThreshold() {
        Function<Wrapper<?>, Integer> accessor = w -> 15;
        Domain<Integer> domain = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.moreThan(accessor, domain, -10));
    }

    @Test
    public void testExactInvalidMinimum() {
        Function<Wrapper<?>, Integer> accessor = w -> 7;
        Domain<Integer> domain = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.exact(accessor, domain, -5, 10));
    }

    @Test
    public void testExactInvalidMaximum() {
        Function<Wrapper<?>, Integer> accessor = w -> 7;
        Domain<Integer> domain = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.exact(accessor, domain, 5, -10));
    }

    @Test
    public void testExactMinimumOverMaximum() {
        Function<Wrapper<?>, Integer> accessor = w -> 7;
        Domain<Integer> domain = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.exact(accessor, domain, 10, 5));
    }

    @Test
    public void testRelativeLessThanInvalidThreshold() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.fromRaw(BigDecimal.valueOf(1.5));
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.relativeLessThan(part, sum, threshold));
    }

    @Test
    public void testRelativeMoreThanInvalidThreshold() {
        Function<Wrapper<?>, Integer> part = w -> 7;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.fromRaw(BigDecimal.valueOf(1.5));
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.relativeMoreThan(part, sum, threshold));
    }

    @Test
    public void testRelativeExactInvalidMinimum() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio minimum = Ratio.fromRaw(BigDecimal.valueOf(1.5));
        Ratio maximum = Ratio.fromRaw(BigDecimal.valueOf(0.7));
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.relativeExact(part, sum, minimum, maximum));
    }

    @Test
    public void testRelativeExactInvalidMaximum() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio minimum = Ratio.fromRaw(BigDecimal.valueOf(0.3));
        Ratio maximum = Ratio.fromRaw(BigDecimal.valueOf(1.5));
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.relativeExact(part, sum, minimum, maximum));
    }

    @Test
    public void testRelativeExactMinimumOverMaximum() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio minimum = Ratio.fromRaw(BigDecimal.valueOf(0.7));
        Ratio maximum = Ratio.fromRaw(BigDecimal.valueOf(0.3));
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.relativeExact(part, sum, minimum, maximum));
    }

    // Helper class for testing
    private static class Domain<T extends Number & Comparable<T>> implements Predicate<T> {
        @Override
        public boolean test(T value) {
            return true;
        }
    }
}
