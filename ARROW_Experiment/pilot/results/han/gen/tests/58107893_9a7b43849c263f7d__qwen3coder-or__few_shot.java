package com.github.robozonky.strategy.natural.conditions;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.robozonky.api.Ratio;
import com.github.robozonky.strategy.natural.Wrapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class RangeCondition_ArrowTest {

    @Test
    public void testLessThan() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> allowedValues = new Domain<Integer>() {
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
        Domain<Integer> allowedValues = new Domain<Integer>() {
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
        Domain<Integer> allowedValues = new Domain<Integer>() {
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
        Ratio threshold = Ratio.from(3, 10);
        RangeCondition<Ratio> condition = RangeCondition.relativeLessThan(part, sum, threshold);
        assertNotNull(condition);
        assertEquals("Relative range: (-inf.; 3/10)", condition.toString());
    }

    @Test
    public void testRelativeMoreThan() {
        Function<Wrapper<?>, Integer> part = w -> 7;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.from(3, 10);
        RangeCondition<Ratio> condition = RangeCondition.relativeMoreThan(part, sum, threshold);
        assertNotNull(condition);
        assertEquals("Relative range: (3/10; +inf.)", condition.toString());
    }

    @Test
    public void testRelativeExact() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio minimum = Ratio.from(1, 10);
        Ratio maximum = Ratio.from(1, 2);
        RangeCondition<Ratio> condition = RangeCondition.relativeExact(part, sum, minimum, maximum);
        assertNotNull(condition);
        assertEquals("Relative range: <1/10; 1/2>", condition.toString());
    }

    @Test
    public void testLessThanWithInvalidThreshold() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> allowedValues = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        try {
            RangeCondition.lessThan(accessor, allowedValues, -5);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testMoreThanWithInvalidThreshold() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> allowedValues = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        try {
            RangeCondition.moreThan(accessor, allowedValues, -5);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testExactWithInvalidMinimum() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> allowedValues = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        try {
            RangeCondition.exact(accessor, allowedValues, -5, 10);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testExactWithInvalidMaximum() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> allowedValues = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        try {
            RangeCondition.exact(accessor, allowedValues, 5, -10);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testExactWithMinimumOverMaximum() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> allowedValues = new Domain<Integer>() {
            @Override
            public boolean test(Integer value) {
                return value >= 0;
            }
        };
        try {
            RangeCondition.exact(accessor, allowedValues, 10, 5);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testRelativeLessThanWithInvalidThreshold() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.from(11, 10); // Invalid: greater than 1
        try {
            RangeCondition.relativeLessThan(part, sum, threshold);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testRelativeMoreThanWithInvalidThreshold() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.from(11, 10); // Invalid: greater than 1
        try {
            RangeCondition.relativeMoreThan(part, sum, threshold);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testRelativeExactWithInvalidMinimum() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio minimum = Ratio.from(11, 10); // Invalid: greater than 1
        Ratio maximum = Ratio.from(1, 2);
        try {
            RangeCondition.relativeExact(part, sum, minimum, maximum);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testRelativeExactWithInvalidMaximum() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio minimum = Ratio.from(1, 10);
        Ratio maximum = Ratio.from(11, 10); // Invalid: greater than 1
        try {
            RangeCondition.relativeExact(part, sum, minimum, maximum);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testRelativeExactWithMinimumOverMaximum() {
        Function<Wrapper<?>, Integer> part = w -> 3;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio minimum = Ratio.from(1, 2);
        Ratio maximum = Ratio.from(1, 10);
        try {
            RangeCondition.relativeExact(part, sum, minimum, maximum);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testPredicateEvaluation() {
        Function<Wrapper<?>, Integer> accessor = w -> 7;
        Domain<Integer> allowedValues = new Domain<Integer>() {
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
        };
        
        assertTrue(condition.test(mockWrapper));
    }

    @Test
    public void testGetActualValue() {
        Integer part = 3;
        Integer sum = 10;
        Ratio ratio = RangeCondition.getActualValue(part, sum);
        assertEquals(Ratio.from(3, 10), ratio);
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
        };
        Ratio ratio = RangeCondition.getValueAccessor(mockWrapper, part, sum);
        assertEquals(Ratio.from(3, 10), ratio);
    }
}
