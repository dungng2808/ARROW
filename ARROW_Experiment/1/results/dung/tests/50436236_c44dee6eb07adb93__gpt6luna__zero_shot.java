package com.hartwig.hmftools.common.amber;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TumorContaminationModel_ArrowTest {

    @Test
    public void contaminationReturnsZeroWhenThereAreTooFewThreePlusReads() {
        TumorContaminationModel model = new TumorContaminationModel();
        Map<Integer, Long> altSupportMap = new HashMap<Integer, Long>();
        altSupportMap.put(3, 1999L);
        altSupportMap.put(2, 10L);

        assertEquals(0.0, model.contamination(30L, altSupportMap), 0.0);
    }

    @Test
    public void readsSumsCountsAtOrAboveMinimumSupport() {
        Map<Integer, Long> altSupportMap = new HashMap<Integer, Long>();
        altSupportMap.put(1, 5L);
        altSupportMap.put(2, 7L);
        altSupportMap.put(3, 11L);

        assertEquals(18L, TumorContaminationModel.reads(2, altSupportMap));
    }

    @Test
    public void readsReturnsZeroForEmptyMap() {
        assertEquals(0L, TumorContaminationModel.reads(2, Collections.<Integer, Long>emptyMap()));
    }

    @Test
    public void medianDepthReturnsMiddlePositiveDepth() {
        assertEquals(20, TumorContaminationModel.medianDepth(Collections.<TumorContamination>emptyList()));
    }
}
