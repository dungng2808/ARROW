package br.edu.utfpr.cm.minerador.services.util;

import br.edu.utfpr.cm.minerador.services.matrix.model.Version;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionUtil_ArrowTest {

    @Test
    public void getMaxVersionSequenceReturnsLongestContiguousSequence() {
        List<Version> occurrences = new ArrayList<>(Arrays.asList(
                new Version("1.0"), new Version("1.2"), new Version("1.1"), new Version("1.4")));
        List<Version> allVersions = new ArrayList<>(Arrays.asList(
                new Version("1.4"), new Version("1.2"), new Version("1.0"),
                new Version("1.3"), new Version("1.1")));

        List<Version> result = VersionUtil.getMaxVersionSequence(occurrences, allVersions);

        assertEquals(Arrays.asList(new Version("1.0"), new Version("1.1"), new Version("1.2")), result);
    }

    @Test
    public void getMaxVersionSequenceReturnsEmptyWhenThereAreNoOccurrences() {
        List<Version> result = VersionUtil.getMaxVersionSequence(
                new ArrayList<Version>(), new ArrayList<Version>(Arrays.asList(new Version("1.0"))));

        assertEquals(new ArrayList<Version>(), result);
    }

    @Test
    public void getMaxVersionSequenceWithCountsFiltersBelowMinimum() {
        Map<Version, AtomicInteger> occurrences = new LinkedHashMap<>();
        occurrences.put(new Version("1.0"), new AtomicInteger(2));
        occurrences.put(new Version("1.1"), new AtomicInteger(1));
        occurrences.put(new Version("1.2"), new AtomicInteger(2));
        List<Version> allVersions = new ArrayList<>(Arrays.asList(
                new Version("1.2"), new Version("1.1"), new Version("1.0")));

        List<Version> result = VersionUtil.getMaxVersionSequence(occurrences, allVersions, 2);

        assertEquals(Arrays.asList(new Version("1.2")), result);
    }

    @Test
    public void listStringToListVersionConvertsEachString() {
        List<Version> result = VersionUtil.listStringToListVersion(Arrays.asList("1.0", "2.3"));

        assertEquals(Arrays.asList(new Version("1.0"), new Version("2.3")), result);
    }

    @Test
    public void listStringToListVersionReturnsEmptyForEmptyInput() {
        List<Version> result = VersionUtil.listStringToListVersion(new ArrayList<String>());

        assertEquals(new ArrayList<Version>(), result);
    }
}
