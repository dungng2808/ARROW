package org.semver4j;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RangesList_ArrowTest {

    @Test
    public void shouldAddRangesToList() {
        // Given
        RangesList rangesList = new RangesList();
        List<Range> ranges = Arrays.asList(mock(Range.class), mock(Range.class));

        // When
        RangesList result = rangesList.add(ranges);

        // Then
        assertThat(result).isSameAs(rangesList);
        assertThat(rangesList.get()).hasSize(1);
        assertThat(rangesList.get().get(0)).isSameAs(ranges);
    }

    @Test
    public void shouldNotAddEmptyRangesToList() {
        // Given
        RangesList rangesList = new RangesList();

        // When
        RangesList result = rangesList.add(Collections.emptyList());

        // Then
        assertThat(result).isSameAs(rangesList);
        assertThat(rangesList.get()).isEmpty();
    }

    @Test
    public void shouldReturnRangesList() {
        // Given
        RangesList rangesList = new RangesList();
        List<Range> ranges = Arrays.asList(mock(Range.class), mock(Range.class));
        rangesList.add(ranges);

        // When
        List<List<Range>> result = rangesList.get();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(ranges);
    }

    @Test
    public void shouldBeSatisfiedByAnyWhenAllRangesAreSatisfiedByAny() {
        // Given
        RangesList rangesList = new RangesList();
        Range range1 = mock(Range.class);
        Range range2 = mock(Range.class);
        when(range1.isSatisfiedByAny()).thenReturn(true);
        when(range2.isSatisfiedByAny()).thenReturn(true);
        rangesList.add(Arrays.asList(range1, range2));

        // When
        boolean result = rangesList.isSatisfiedByAny();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldNotBeSatisfiedByAnyWhenOneRangeIsNotSatisfiedByAny() {
        // Given
        RangesList rangesList = new RangesList();
        Range range1 = mock(Range.class);
        Range range2 = mock(Range.class);
        when(range1.isSatisfiedByAny()).thenReturn(true);
        when(range2.isSatisfiedByAny()).thenReturn(false);
        rangesList.add(Arrays.asList(range1, range2));

        // When
        boolean result = rangesList.isSatisfiedByAny();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldNotBeSatisfiedByAnyWhenNoRangesExist() {
        // Given
        RangesList rangesList = new RangesList();

        // When
        boolean result = rangesList.isSatisfiedByAny();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldNotBeSatisfiedByVersionWhenNoRangesExist() {
        // Given
        RangesList rangesList = new RangesList();
        Semver version = mock(Semver.class);

        // When
        boolean result = rangesList.isSatisfiedBy(version);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldNotBeSatisfiedByVersionWhenNoRangeInSetIsSatisfied() {
        // Given
        RangesList rangesList = new RangesList();
        Range range1 = mock(Range.class);
        Range range2 = mock(Range.class);
        Semver version = mock(Semver.class);
        when(range1.isSatisfiedBy(version)).thenReturn(false);
        when(range2.isSatisfiedBy(version)).thenReturn(false);
        rangesList.add(Arrays.asList(range1, range2));

        // When
        boolean result = rangesList.isSatisfiedBy(version);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    public void shouldBeSatisfiedByVersionWhenOneSetOfRangesIsSatisfied() {
        // Given
        RangesList rangesList = new RangesList();
        Range range1 = mock(Range.class);
        Range range2 = mock(Range.class);
        Semver version = mock(Semver.class);
        when(range1.isSatisfiedBy(version)).thenReturn(true);
        when(range2.isSatisfiedBy(version)).thenReturn(true);
        rangesList.add(Arrays.asList(range1, range2));

        // When
        boolean result = rangesList.isSatisfiedBy(version);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldFormatToStringWithSingleRange() {
        // Given
        RangesList rangesList = new RangesList();
        Range range = mock(Range.class);
        when(range.toString()).thenReturn(">=1.0.0");
        rangesList.add(Arrays.asList(range));

        // When
        String result = rangesList.toString();

        // Then
        assertThat(result).isEqualTo(">=1.0.0");
    }

    @Test
    public void shouldFormatToStringWithMultipleRanges() {
        // Given
        RangesList rangesList = new RangesList();
        Range range1 = mock(Range.class);
        Range range2 = mock(Range.class);
        when(range1.toString()).thenReturn(">=1.0.0");
        when(range2.toString()).thenReturn("<=2.0.0");
        rangesList.add(Arrays.asList(range1, range2));

        // When
        String result = rangesList.toString();

        // Then
        assertThat(result).isEqualTo("(>=1.0.0 and <=2.0.0)");
    }

    @Test
    public void shouldFormatToStringWithMultipleSets() {
        // Given
        RangesList rangesList = new RangesList();
        Range range1 = mock(Range.class);
        Range range2 = mock(Range.class);
        Range range3 = mock(Range.class);
        when(range1.toString()).thenReturn(">=1.0.0");
        when(range2.toString()).thenReturn("<=2.0.0");
        when(range3.toString()).thenReturn(">=3.0.0");
        rangesList.add(Arrays.asList(range1, range2));
        rangesList.add(Arrays.asList(range3));

        // When
        String result = rangesList.toString();

        // Then
        assertThat(result).isEqualTo("(>=1.0.0 and <=2.0.0) or >=3.0.0");
    }

    @Test
    public void shouldFormatToStringWithNestedParentheses() {
        // Given
        RangesList rangesList = new RangesList();
        Range range1 = mock(Range.class);
        Range range2 = mock(Range.class);
        when(range1.toString()).thenReturn(">=1.0.0");
        when(range2.toString()).thenReturn("<=2.0.0");
        rangesList.add(Arrays.asList(range1, range2));

        // When
        String result = rangesList.toString();

        // Then
        assertThat(result).isEqualTo("(>=1.0.0 and <=2.0.0)");
    }
}
