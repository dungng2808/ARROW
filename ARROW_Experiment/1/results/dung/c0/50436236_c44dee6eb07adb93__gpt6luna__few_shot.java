package com.hartwig.hmftools.common.amber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TumorContaminationModel_ArrowTest {

    @Test
    public void contaminationReturnsZeroWhenThereAreTooFewThreePlusReads() {
        TumorContaminationModel model = new TumorContaminationModel();
        Map<Integer, Long> altSupportMap = new HashMap<>();
        altSupportMap.put(3, 1999L);

        assertEquals(0.0, model.contamination(30L, altSupportMap), 0.0);
    }

    @Test
    public void readsSumsCountsAtOrAboveMinimumSupport() {
        Map<Integer, Long> altSupportMap = new HashMap<>();
        altSupportMap.put(1, 10L);
        altSupportMap.put(2, 20L);
        altSupportMap.put(3, 30L);

        assertEquals(50L, TumorContaminationModel.reads(2, altSupportMap));
    }

    @Test
    public void readsReturnsZeroForEmptyMap() {
        assertEquals(0L, TumorContaminationModel.reads(2, Collections.<Integer, Long>emptyMap()));
    }

    @Test
    public void medianDepthReturnsMedianPositiveTumorDepth() {
        List<TumorContamination> contamination = Arrays.asList(
                new TumorContamination(null, null, null, null, 10),
                new TumorContamination(null, null, null, null, 20),
                new TumorContamination(null, null, null, null, 30));

        assertEquals(20, TumorContaminationModel.medianDepth(contamination));
    }

    @Test
    public void contaminationReturnsZeroWhenNoSitesPassNormalDepthFilter() {
        TumorContaminationModel model = new TumorContaminationModel();
        assertEquals(0.0, model.contamination(Collections.<TumorContamination>emptyList()), 0.0);
    }
}
