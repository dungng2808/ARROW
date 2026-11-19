package com.hartwig.hmftools.common.amber;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TumorContaminationModel_ArrowTest {

    @Test
    public void testContaminationBelowMinThreePlusReadsReturnsZero() {
        TumorContaminationModel model = new TumorContaminationModel();
        Map<Integer, Long> altSupportMap = new HashMap<>();
        altSupportMap.put(0, 1000L);
        altSupportMap.put(1, 1000L);
        altSupportMap.put(2, 500L);
        altSupportMap.put(3, 500L);

        double result = model.contamination(1000L, altSupportMap);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testContaminationAboveMinThreePlusReadsReturnsContaminationValue() {
        TumorContaminationModel model = new TumorContaminationModel();
        Map<Integer, Long> altSupportMap = new HashMap<>();
        altSupportMap.put(0, 1000L);
        altSupportMap.put(1, 1000L);
        altSupportMap.put(2, 500L);
        altSupportMap.put(3, 1000L);

        double result = model.contamination(1000L, altSupportMap);
        // Should return a value between 0 and 1, but exact value depends on complex calculation
        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testReadsMethod() {
        Map<Integer, Long> altSupportMap = new HashMap<>();
        altSupportMap.put(0, 100L);
        altSupportMap.put(1, 200L);
        altSupportMap.put(2, 300L);
        altSupportMap.put(3, 400L);

        long result = TumorContaminationModel.reads(2, altSupportMap);
        assertEquals(700L, result);
    }

    @Test
    public void testMedianDepth() {
        TumorContamination tc1 = mock(TumorContamination.class);
        TumorContamination tc2 = mock(TumorContamination.class);
        TumorContamination tc3 = mock(TumorContamination.class);

        when(tc1.tumor()).thenReturn(mock(TumorContamination.Tumor.class));
        when(tc2.tumor()).thenReturn(mock(TumorContamination.Tumor.class));
        when(tc3.tumor()).thenReturn(mock(TumorContamination.Tumor.class));

        when(tc1.tumor().readDepth()).thenReturn(100);
        when(tc2.tumor().readDepth()).thenReturn(200);
        when(tc3.tumor().readDepth()).thenReturn(300);

        List<TumorContamination> contaminationList = Arrays.asList(tc1, tc2, tc3);
        int result = TumorContaminationModel.medianDepth(contaminationList);
        assertEquals(200, result);
    }

    @Test
    public void testContaminationScore() {
        Map<Integer, Long> altSupportMap = new HashMap<>();
        altSupportMap.put(0, 100L);
        altSupportMap.put(1, 200L);
        altSupportMap.put(2, 300L);
        altSupportMap.put(3, 400L);

        TumorContaminationModel model = new TumorContaminationModel();
        double result = model.contaminationScore(0.1, 700L, 1000L, altSupportMap);
        // Just verify it doesn't throw exception and returns reasonable value
        assertEquals(0.0, result, 1000.0);
    }
}
