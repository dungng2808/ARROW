package br.edu.utfpr.cm.minerador.services.util;

import br.edu.utfpr.cm.minerador.services.matrix.model.Version;
import br.edu.utfpr.minerador.preprocessor.comparator.VersionComparator;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class VersionUtil_ArrowTest {

    @Test
    public void testGetMaxVersionSequence_NormalCase() {
        List<Version> ocurrencesVersions = new ArrayList<>();
        ocurrencesVersions.add(new Version("1.0"));
        ocurrencesVersions.add(new Version("1.1"));
        ocurrencesVersions.add(new Version("1.2"));

        List<Version> allVersions = new ArrayList<>();
        allVersions.add(new Version("1.0"));
        allVersions.add(new Version("1.1"));
        allVersions.add(new Version("1.2"));
        allVersions.add(new Version("1.3"));

        List<Version> result = VersionUtil.getMaxVersionSequence(ocurrencesVersions, allVersions);
        
        assertEquals(3, result.size());
        assertEquals("1.0", result.get(0).getVersion());
        assertEquals("1.1", result.get(1).getVersion());
        assertEquals("1.2", result.get(2).getVersion());
    }

    @Test
    public void testGetMaxVersionSequence_WithGaps() {
        List<Version> ocurrencesVersions = new ArrayList<>();
        ocurrencesVersions.add(new Version("1.0"));
        ocurrencesVersions.add(new Version("1.2"));
        ocurrencesVersions.add(new Version("1.3"));

        List<Version> allVersions = new ArrayList<>();
        allVersions.add(new Version("1.0"));
        allVersions.add(new Version("1.1"));
        allVersions.add(new Version("1.2"));
        allVersions.add(new Version("1.3"));

        List<Version> result = VersionUtil.getMaxVersionSequence(ocurrencesVersions, allVersions);
        
        assertEquals(2, result.size());
        assertEquals("1.2", result.get(0).getVersion());
        assertEquals("1.3", result.get(1).getVersion());
    }

    @Test
    public void testGetMaxVersionSequence_EmptyLists() {
        List<Version> ocurrencesVersions = new ArrayList<>();
        List<Version> allVersions = new ArrayList<>();
        
        List<Version> result = VersionUtil.getMaxVersionSequence(ocurrencesVersions, allVersions);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetMaxVersionSequence_SingleVersion() {
        List<Version> ocurrencesVersions = new ArrayList<>();
        ocurrencesVersions.add(new Version("1.0"));

        List<Version> allVersions = new ArrayList<>();
        allVersions.add(new Version("1.0"));
        allVersions.add(new Version("1.1"));

        List<Version> result = VersionUtil.getMaxVersionSequence(ocurrencesVersions, allVersions);
        
        assertEquals(1, result.size());
        assertEquals("1.0", result.get(0).getVersion());
    }

    @Test
    public void testGetMaxVersionSequence_NoConsecutiveVersions() {
        List<Version> ocurrencesVersions = new ArrayList<>();
        ocurrencesVersions.add(new Version("1.0"));
        ocurrencesVersions.add(new Version("1.3"));

        List<Version> allVersions = new ArrayList<>();
        allVersions.add(new Version("1.0"));
        allVersions.add(new Version("1.1"));
        allVersions.add(new Version("1.2"));
        allVersions.add(new Version("1.3"));

        List<Version> result = VersionUtil.getMaxVersionSequence(ocurrencesVersions, allVersions);
        
        assertEquals(1, result.size());
        assertEquals("1.0", result.get(0).getVersion());
    }

    @Test
    public void testGetMaxVersionSequence_MapVersionWithMinOccurrences() {
        Map<Version, AtomicInteger> ocurrencesVersions = new HashMap<>();
        ocurrencesVersions.put(new Version("1.0"), new AtomicInteger(2));
        ocurrencesVersions.put(new Version("1.1"), new AtomicInteger(1));
        ocurrencesVersions.put(new Version("1.2"), new AtomicInteger(3));

        List<Version> allVersions = new ArrayList<>();
        allVersions.add(new Version("1.0"));
        allVersions.add(new Version("1.1"));
        allVersions.add(new Version("1.2"));
        allVersions.add(new Version("1.3"));

        List<Version> result = VersionUtil.getMaxVersionSequence(ocurrencesVersions, allVersions, 1);
        
        assertEquals(3, result.size());
        assertEquals("1.0", result.get(0).getVersion());
        assertEquals("1.1", result.get(1).getVersion());
        assertEquals("1.2", result.get(2).getVersion());
    }

    @Test
    public void testGetMaxVersionSequence_MapVersionWithMinOccurrencesFiltering() {
        Map<Version, AtomicInteger> ocurrencesVersions = new HashMap<>();
        ocurrencesVersions.put(new Version("1.0"), new AtomicInteger(1));
        ocurrencesVersions.put(new Version("1.1"), new AtomicInteger(2));
        ocurrencesVersions.put(new Version("1.2"), new AtomicInteger(1));

        List<Version> allVersions = new ArrayList<>();
        allVersions.add(new Version("1.0"));
        allVersions.add(new Version("1.1"));
        allVersions.add(new Version("1.2"));
        allVersions.add(new Version("1.3"));

        List<Version> result = VersionUtil.getMaxVersionSequence(ocurrencesVersions, allVersions, 2);
        
        assertEquals(1, result.size());
        assertEquals("1.1", result.get(0).getVersion());
    }

    @Test
    public void testGetMaxVersionSequence_MapEmpty() {
        Map<Version, AtomicInteger> ocurrencesVersions = new HashMap<>();
        List<Version> allVersions = new ArrayList<>();
        allVersions.add(new Version("1.0"));
        allVersions.add(new Version("1.1"));

        List<Version> result = VersionUtil.getMaxVersionSequence(ocurrencesVersions, allVersions, 1);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testListStringToListVersion_NormalCase() {
        List<String> versions = new ArrayList<>();
        versions.add("1.0");
        versions.add("1.1");
        versions.add("1.2");

        List<Version> result = VersionUtil.listStringToListVersion(versions);
        
        assertEquals(3, result.size());
        assertEquals("1.0", result.get(0).getVersion());
        assertEquals("1.1", result.get(1).getVersion());
        assertEquals("1.2", result.get(2).getVersion());
    }

    @Test
    public void testListStringToListVersion_EmptyList() {
        List<String> versions = new ArrayList<>();
        
        List<Version> result = VersionUtil.listStringToListVersion(versions);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
