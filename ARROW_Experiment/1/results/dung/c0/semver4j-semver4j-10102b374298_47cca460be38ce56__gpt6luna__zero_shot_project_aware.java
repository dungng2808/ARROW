package org.semver4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RangesList_ArrowTest {

    @Test
    public void addStoresNonEmptyRangesAndReturnsThis() {
        RangesList rangesList = new RangesList();
        List<Range> ranges = Collections.singletonList(new Range(">=1.0.0"));

        RangesList result = rangesList.add(ranges);

        assertThat(result).isSameAs(rangesList);
        assertThat(rangesList.get()).containsExactly(ranges);
    }

    @Test
    public void addIgnoresEmptyRanges() {
        RangesList rangesList = new RangesList();

        rangesList.add(Collections.<Range>emptyList());

        assertThat(rangesList.get()).isEmpty();
    }

    @Test
    public void getReturnsMutableRangesList() {
        RangesList rangesList = new RangesList();
        List<Range> ranges = Collections.singletonList(new Range(">=1.0.0"));

        rangesList.get().add(ranges);

        assertThat(rangesList.get()).containsExactly(ranges);
    }

    @Test
    public void isSatisfiedByAnyReturnsTrueForEmptyRangesList() {
        assertThat(new RangesList().isSatisfiedByAny()).isTrue();
    }

    @Test
    public void isSatisfiedByAnyChecksAllRanges() {
        RangesList rangesList = new RangesList()
            .add(Collections.singletonList(new Range(">=1.0.0")))
            .add(Collections.singletonList(new Range("<0.0.0")));

        assertThat(rangesList.isSatisfiedByAny()).isFalse();
    }

    @Test
    public void isSatisfiedByMatchesAnySetOfRanges() {
        RangesList rangesList = new RangesList()
            .add(Collections.singletonList(new Range("<1.0.0")))
            .add(Collections.singletonList(new Range(">=2.0.0")));

        assertThat(rangesList.isSatisfiedBy(new Semver("2.0.0"))).isTrue();
        assertThat(rangesList.isSatisfiedBy(new Semver("1.5.0"))).isFalse();
    }

    @Test
    public void isSatisfiedByRequiresEveryRangeInASet() {
        RangesList rangesList = new RangesList()
            .add(Arrays.asList(new Range(">=1.0.0"), new Range("<2.0.0")));

        assertThat(rangesList.isSatisfiedBy(new Semver("1.5.0"))).isTrue();
        assertThat(rangesList.isSatisfiedBy(new Semver("2.0.0"))).isFalse();
    }

    @Test
    public void isSatisfiedByRequiresMatchingPrereleaseRangeAtSameBaseVersion() {
        RangesList rangesList = new RangesList()
            .add(Collections.singletonList(new Range(">=1.0.0")));

        assertThat(rangesList.isSatisfiedBy(new Semver("1.0.0-alpha"))).isFalse();
    }

    @Test
    public void isSatisfiedByAllowsPrereleaseWhenRangeHasPrereleaseAtSameBaseVersion() {
        RangesList rangesList = new RangesList()
            .add(Collections.singletonList(new Range(">=1.0.0-alpha")));

        assertThat(rangesList.isSatisfiedBy(new Semver("1.0.0-beta"))).isTrue();
    }

    @Test
    public void toStringFormatsSingleAndMultipleRangeSets() {
        RangesList rangesList = new RangesList()
            .add(Collections.singletonList(new Range(">=1.0.0")))
            .add(Arrays.asList(new Range(">=2.0.0"), new Range("<3.0.0")));

        assertThat(rangesList.toString()).isEqualTo(">=1.0.0 or (>=2.0.0 and <3.0.0)");
    }

    @Test
    public void toStringIsEmptyForNoRanges() {
        assertThat(new RangesList().toString()).isEmpty();
    }
}
