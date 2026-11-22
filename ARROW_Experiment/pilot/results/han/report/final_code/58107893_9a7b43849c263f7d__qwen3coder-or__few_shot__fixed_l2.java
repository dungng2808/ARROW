package com.github.robozonky.strategy.natural.conditions;

import java.math.BigDecimal;
import java.util.function.Function;

import com.github.robozonky.api.Ratio;
import com.github.robozonky.strategy.natural.Wrapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RangeCondition_ArrowTest {

    @Test
    public void testLessThan() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> allowedValues = new Domain<Integer>(Integer.class, 0, Integer.MAX_VALUE) {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        RangeCondition<Integer> condition = RangeCondition.lessThan(accessor, allowedValues, 10);
        assertNotNull(condition);
        assertEquals("Range: (-inf.; 10)", condition.toString());
    }

    @Test
    public void testMoreThan() {
        Function<Wrapper<?>, Integer> accessor = w -> 15;
        Domain<Integer> allowedValues = new Domain<Integer>(Integer.class, 0, Integer.MAX_VALUE) {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        RangeCondition<Integer> condition = RangeCondition.moreThan(accessor, allowedValues, 10);
        assertNotNull(condition);
        assertEquals("Range: (10; +inf.)", condition.toString());
    }

    @Test
    public void testExact() {
        Function<Wrapper<?>, Integer> accessor = w -> 7;
        Domain<Integer> allowedValues = new Domain<Integer>(Integer.class, 0, Integer.MAX_VALUE) {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        RangeCondition<Integer> condition = RangeCondition.exact(accessor, allowedValues, 5, 10);
        assertNotNull(condition);
        assertEquals("Range: <5; 10>", condition.toString());
    }

    @Test
    public void testRelativeLessThan() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.fromRaw(BigDecimal.valueOf(3).divide(BigDecimal.valueOf(10), 10, BigDecimal.ROUND_HALF_UP));
        RangeCondition<Ratio> condition = RangeCondition.relativeLessThan(part, sum, threshold);
        assertNotNull(condition);
        assertEquals("Relative range: (-inf.; 3/10)", condition.toString());
    }

    @Test
    public void testRelativeMoreThan() {
        Function<Wrapper<?>, Integer> part = w -> 7;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.fromRaw(BigDecimal.valueOf(3).divide(BigDecimal.valueOf(10), 10, BigDecimal.ROUND_HALF_UP));
        RangeCondition<Ratio> condition = RangeCondition.relativeMoreThan(part, sum, threshold);
        assertNotNull(condition);
        assertEquals("Relative range: (3/10; +inf.)", condition.toString());
    }

    @Test
    public void testRelativeExact() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio minimum = Ratio.fromRaw(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(10), 10, BigDecimal.ROUND_HALF_UP));
        Ratio maximum = Ratio.fromRaw(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(2), 10, BigDecimal.ROUND_HALF_UP));
        RangeCondition<Ratio> condition = RangeCondition.relativeExact(part, sum, minimum, maximum);
        assertNotNull(condition);
        assertEquals("Relative range: <1/10; 1/2>", condition.toString());
    }

    @Test
    public void testLessThanWithInvalidThreshold() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> allowedValues = new Domain<Integer>(Integer.class, 0, Integer.MAX_VALUE) {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.lessThan(accessor, allowedValues, -5));
    }

    @Test
    public void testMoreThanWithInvalidThreshold() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> allowedValues = new Domain<Integer>(Integer.class, 0, Integer.MAX_VALUE) {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.moreThan(accessor, allowedValues, -5));
    }

    @Test
    public void testExactWithInvalidMinimum() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> allowedValues = new Domain<Integer>(Integer.class, 0, Integer.MAX_VALUE) {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.exact(accessor, allowedValues, -5, 10));
    }

    @Test
    public void testExactWithInvalidMaximum() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> allowedValues = new Domain<Integer>(Integer.class, 0, Integer.MAX_VALUE) {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.exact(accessor, allowedValues, 5, -10));
    }

    @Test
    public void testExactWithMinimumOverMaximum() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> allowedValues = new Domain<Integer>(Integer.class, 0, Integer.MAX_VALUE) {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.exact(accessor, allowedValues, 10, 5));
    }

    @Test
    public void testRelativeLessThanWithInvalidThreshold() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.fromRaw(BigDecimal.valueOf(11).divide(BigDecimal.valueOf(10), 10, BigDecimal.ROUND_HALF_UP)); // Invalid: greater than 1
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.relativeLessThan(part, sum, threshold));
    }

    @Test
    public void testRelativeMoreThanWithInvalidThreshold() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.fromRaw(BigDecimal.valueOf(11).divide(BigDecimal.valueOf(10), 10, BigDecimal.ROUND_HALF_UP)); // Invalid: greater than 1
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.relativeMoreThan(part, sum, threshold));
    }

    @Test
    public void testRelativeExactWithInvalidMinimum() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio minimum = Ratio.fromRaw(BigDecimal.valueOf(11).divide(BigDecimal.valueOf(10), 10, BigDecimal.ROUND_HALF_UP)); // Invalid: greater than 1
        Ratio maximum = Ratio.fromRaw(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(2), 10, BigDecimal.ROUND_HALF_UP));
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.relativeExact(part, sum, minimum, maximum));
    }

    @Test
    public void testRelativeExactWithInvalidMaximum() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio minimum = Ratio.fromRaw(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(10), 10, BigDecimal.ROUND_HALF_UP));
        Ratio maximum = Ratio.fromRaw(BigDecimal.valueOf(11).divide(BigDecimal.valueOf(10), 10, BigDecimal.ROUND_HALF_UP)); // Invalid: greater than 1
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.relativeExact(part, sum, minimum, maximum));
    }

    @Test
    public void testRelativeExactWithMinimumOverMaximum() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio minimum = Ratio.fromRaw(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(2), 10, BigDecimal.ROUND_HALF_UP));
        Ratio maximum = Ratio.fromRaw(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(10), 10, BigDecimal.ROUND_HALF_UP));
        assertThrows(IllegalArgumentException.class, () -> RangeCondition.relativeExact(part, sum, minimum, maximum));
    }

    @Test
    public void testPredicateEvaluation() {
        Function<Wrapper<?>, Integer> accessor = w -> 7;
        Domain<Integer> allowedValues = new Domain<Integer>(Integer.class, 0, Integer.MAX_VALUE) {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        RangeCondition<Integer> condition = RangeCondition.exact(accessor, allowedValues, 5, 10);
        
        // Mock wrapper for testing
        Wrapper<?> mockWrapper = new Wrapper<Object>() {
            @Override
            public Object get() {
                return null;
            }

            @Override
            public Object getOriginal() {
                return null;
            }

            @Override
            public int saleFee() {
                return 0;
            }
        };
        
        assertTrue(condition.test(mockWrapper));
    }

    @Test
    public void testGetActualValue() {
        Integer part = 3;
        Integer sum = 10;
        Ratio ratio = RangeCondition.getActualValue(part, sum);
        assertEquals(Ratio.fromRaw(BigDecimal.valueOf(3).divide(BigDecimal.valueOf(10), 10, BigDecimal.ROUND_HALF_UP)), ratio);
    }

    @Test
    public void testGetValueAccessor() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Wrapper<?> mockWrapper = new Wrapper<Object>() {
            @Override
            public Object get() {
                return null;
            }

            @Override
            public Object getOriginal() {
                return null;
            }

            @Override
            public int saleFee() {
                return 0;
            }
        };
        Ratio ratio = RangeCondition.getValueAccessor(mockWrapper, part, sum);
        assertEquals(Ratio.fromRaw(BigDecimal.valueOf(3).divide(BigDecimal.valueOf(10), 10, BigDecimal.ROUND_HALF_UP)), ratio);
    }
}
