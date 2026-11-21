package br.edu.utfpr.cm.minerador.services.util;

import br.edu.utfpr.cm.minerador.services.matrix.model.Version;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class VersionUtil_ArrowTest {

    @Test
    public void getMaxVersionSequenceReturnsLongestContiguousSequence() {
        List<Version> occurrences = new ArrayList<>(Arrays.asList(
                new Version("2.0"), new Version("1.0"), new Version("3.0")));
        List<Version> allVersions = new ArrayList<>(Arrays.asList(
                new Version("3.0"), new Version("1.0"), new Version("2.0")));

        List<Version> result = VersionUtil.getMaxVersionSequence(occurrences, allVersions);

        assertEquals(Arrays.asList(new Version("1.0"), new Version("2.0"), new Version("3.0")), result);
    }

    @Test
    public void getMaxVersionSequenceReturnsEmptyForNoOccurrences() {
        List<Version> occurrences = new ArrayList<>();
        List<Version> allVersions = new ArrayList<>(Arrays.asList(new Version("1.0")));

        List<Version> result = VersionUtil.getMaxVersionSequence(occurrences, allVersions);

        assertEquals(new ArrayList<Version>(), result);
    }

    @Test
    public void getMaxVersionSequenceWithCountsFiltersBelowMinimum() {
        List<Version> first = Arrays.asList(new Version("1.0"), new Version("2.0"), new Version("3.0"));
        List<Version> allVersions = new ArrayList<>(first);
        Map<Version, AtomicInteger> occurrences = new LinkedHashMap<>();
        occurrences.put(first.get(0), new AtomicInteger(2));
        occurrences.put(first.get(1), new AtomicInteger(1));
        occurrences.put(first.get(2), new AtomicInteger(2));

        List<Version> result = VersionUtil.getMaxVersionSequence(occurrences, allVersions, 2);

        assertEquals(Arrays.asList(first.get(2)), result);
    }

    @Test
    public void getMaxVersionSequenceWithCountsReturnsContiguousQualifyingVersions() {
        List<Version> first = Arrays.asList(new Version("1.0"), new Version("2.0"), new Version("3.0"));
        List<Version> allVersions = new ArrayList<>(first);
        Map<Version, AtomicInteger> occurrences = new LinkedHashMap<>();
        occurrences.put(first.get(0), new AtomicInteger(3));
        occurrences.put(first.get(1), new AtomicInteger(3));
        occurrences.put(first.get(2), new AtomicInteger(3));

        List<Version> result = VersionUtil.getMaxVersionSequence(occurrences, allVersions, 3);

        assertEquals(first, result);
    }

    @Test
    public void listStringToListVersionConvertsEachString() {
        List<Version> result = VersionUtil.listStringToListVersion(Arrays.asList("1.0", "2.0"));

        assertEquals(Arrays.asList(new Version("1.0"), new Version("2.0")), result);
    }

    @Test
    public void listStringToListVersionReturnsEmptyForEmptyInput() {
        List<Version> result = VersionUtil.listStringToListVersion(new ArrayList<String>());

        assertEquals(new ArrayList<Version>(), result);
    }
}
