package com.github.robozonky.strategy.natural.conditions;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.github.robozonky.api.Ratio;
import com.github.robozonky.strategy.natural.Wrapper;
import com.github.robozonky.test.AbstractRoboZonkyTest;

public class RangeCondition_ArrowTest extends AbstractRoboZonkyTest {

    @Test
    public void testLessThan() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> domain = mock(Domain.class);
        when(domain.test(any())).thenReturn(true);
        Integer threshold = 10;
        
        RangeCondition<Integer> condition = RangeCondition.lessThan(accessor, domain, threshold);
        
        assertThat(condition).isNotNull();
        assertThat(condition.toString()).isEqualTo("Range: (-inf.; 10)");
        
        // Test the predicate
        Wrapper<?> wrapper = mock(Wrapper.class);
        when(wrapper.getValue()).thenReturn(5);
        assertThat(condition.test(wrapper)).isTrue();
        
        when(wrapper.getValue()).thenReturn(15);
        assertThat(condition.test(wrapper)).isFalse();
    }
    
    @Test
    public void testMoreThan() {
        Function<Wrapper<?>, Integer> accessor = w -> 15;
        Domain<Integer> domain = mock(Domain.class);
        when(domain.test(any())).thenReturn(true);
        Integer threshold = 10;
        
        RangeCondition<Integer> condition = RangeCondition.moreThan(accessor, domain, threshold);
        
        assertThat(condition).isNotNull();
        assertThat(condition.toString()).isEqualTo("Range: (10; +inf.)");
        
        // Test the predicate
        Wrapper<?> wrapper = mock(Wrapper.class);
        when(wrapper.getValue()).thenReturn(15);
        assertThat(condition.test(wrapper)).isTrue();
        
        when(wrapper.getValue()).thenReturn(5);
        assertThat(condition.test(wrapper)).isFalse();
    }
    
    @Test
    public void testExact() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> domain = mock(Domain.class);
        when(domain.test(any())).thenReturn(true);
        Integer min = 2;
        Integer max = 10;
        
        RangeCondition<Integer> condition = RangeCondition.exact(accessor, domain, min, max);
        
        assertThat(condition).isNotNull();
        assertThat(condition.toString()).isEqualTo("Range: <2; 10>");
        
        // Test the predicate
        Wrapper<?> wrapper = mock(Wrapper.class);
        when(wrapper.getValue()).thenReturn(5);
        assertThat(condition.test(wrapper)).isTrue();
        
        when(wrapper.getValue()).thenReturn(1);
        assertThat(condition.test(wrapper)).isFalse();
        
        when(wrapper.getValue()).thenReturn(15);
        assertThat(condition.test(wrapper)).isFalse();
    }
    
    @Test
    public void testRelativeLessThan() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.from(0.5);
        
        RangeCondition<Ratio> condition = RangeCondition.relativeLessThan(part, sum, threshold);
        
        assertThat(condition).isNotNull();
        assertThat(condition.toString()).isEqualTo("Relative range: (-inf.; 0.5)");
        
        // Test the predicate
        Wrapper<?> wrapper = mock(Wrapper.class);
        when(part.apply(wrapper)).thenReturn(5);
        when(sum.apply(wrapper)).thenReturn(10);
        assertThat(condition.test(wrapper)).isFalse(); // 5/10 = 0.5, not less than 0.5
        
        when(part.apply(wrapper)).thenReturn(4);
        when(sum.apply(wrapper)).thenReturn(10);
        assertThat(condition.test(wrapper)).isTrue(); // 4/10 = 0.4, less than 0.5
    }
    
    @Test
    public void testRelativeMoreThan() {
        Function<Wrapper<?>, Integer> part = w -> 6;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.from(0.5);
        
        RangeCondition<Ratio> condition = RangeCondition.relativeMoreThan(part, sum, threshold);
        
        assertThat(condition).isNotNull();
        assertThat(condition.toString()).isEqualTo("Relative range: (0.5; +inf.)");
        
        // Test the predicate
        Wrapper<?> wrapper = mock(Wrapper.class);
        when(part.apply(wrapper)).thenReturn(6);
        when(sum.apply(wrapper)).thenReturn(10);
        assertThat(condition.test(wrapper)).isTrue(); // 6/10 = 0.6, greater than 0.5
        
        when(part.apply(wrapper)).thenReturn(4);
        when(sum.apply(wrapper)).thenReturn(10);
        assertThat(condition.test(wrapper)).isFalse(); // 4/10 = 0.4, not greater than 0.5
    }
    
    @Test
    public void testRelativeExact() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio min = Ratio.from(0.3);
        Ratio max = Ratio.from(0.7);
        
        RangeCondition<Ratio> condition = RangeCondition.relativeExact(part, sum, min, max);
        
        assertThat(condition).isNotNull();
        assertThat(condition.toString()).isEqualTo("Relative range: <0.3; 0.7>");
        
        // Test the predicate
        Wrapper<?> wrapper = mock(Wrapper.class);
        when(part.apply(wrapper)).thenReturn(5);
        when(sum.apply(wrapper)).thenReturn(10);
        assertThat(condition.test(wrapper)).isTrue(); // 5/10 = 0.5, between 0.3 and 0.7
        
        when(part.apply(wrapper)).thenReturn(2);
        when(sum.apply(wrapper)).thenReturn(10);
        assertThat(condition.test(wrapper)).isFalse(); // 2/10 = 0.2, less than 0.3
        
        when(part.apply(wrapper)).thenReturn(8);
        when(sum.apply(wrapper)).thenReturn(10);
        assertThat(condition.test(wrapper)).isFalse(); // 8/10 = 0.8, greater than 0.7
    }
    
    @Test
    public void testLessThanWithInvalidThreshold() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> domain = mock(Domain.class);
        when(domain.test(any())).thenReturn(false);
        Integer threshold = 10;
        
        assertThatThrownBy(() -> RangeCondition.lessThan(accessor, domain, threshold))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testMoreThanWithInvalidThreshold() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> domain = mock(Domain.class);
        when(domain.test(any())).thenReturn(false);
        Integer threshold = 10;
        
        assertThatThrownBy(() -> RangeCondition.moreThan(accessor, domain, threshold))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testExactWithInvalidMinimum() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> domain = mock(Domain.class);
        when(domain.test(any())).thenReturn(false);
        Integer min = 2;
        Integer max = 10;
        
        assertThatThrownBy(() -> RangeCondition.exact(accessor, domain, min, max))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testExactWithInvalidMaximum() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> domain = mock(Domain.class);
        when(domain.test(any())).thenReturn(true);
        when(domain.test(eq(2))).thenReturn(true);
        when(domain.test(eq(10))).thenReturn(false);
        Integer min = 2;
        Integer max = 10;
        
        assertThatThrownBy(() -> RangeCondition.exact(accessor, domain, min, max))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testExactWithMinOverMax() {
        Function<Wrapper<?>, Integer> accessor = w -> 5;
        Domain<Integer> domain = mock(Domain.class);
        when(domain.test(any())).thenReturn(true);
        Integer min = 10;
        Integer max = 2;
        
        assertThatThrownBy(() -> RangeCondition.exact(accessor, domain, min, max))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testRelativeLessThanWithInvalidThreshold() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.from(1.5);
        
        assertThatThrownBy(() -> RangeCondition.relativeLessThan(part, sum, threshold))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testRelativeMoreThanWithInvalidThreshold() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio threshold = Ratio.from(1.5);
        
        assertThatThrownBy(() -> RangeCondition.relativeMoreThan(part, sum, threshold))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testRelativeExactWithInvalidMinimum() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio min = Ratio.from(1.5);
        Ratio max = Ratio.from(0.7);
        
        assertThatThrownBy(() -> RangeCondition.relativeExact(part, sum, min, max))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testRelativeExactWithInvalidMaximum() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio min = Ratio.from(0.3);
        Ratio max = Ratio.from(1.5);
        
        assertThatThrownBy(() -> RangeCondition.relativeExact(part, sum, min, max))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testRelativeExactWithMinOverMax() {
        Function<Wrapper<?>, Integer> part = w -> 5;
        Function<Wrapper<?>, Integer> sum = w -> 10;
        Ratio min = Ratio.from(0.7);
        Ratio max = Ratio.from(0.3);
        
        assertThatThrownBy(() -> RangeCondition.relativeExact(part, sum, min, max))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
