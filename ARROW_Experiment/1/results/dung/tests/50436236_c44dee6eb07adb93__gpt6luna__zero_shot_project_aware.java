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

        assertEquals(0.0, model.contamination(30L, altSupportMap), 0.0);
    }

    @Test
    public void contaminationReturnsZeroForEmptyAltSupportMap() {
        TumorContaminationModel model = new TumorContaminationModel();

        assertEquals(0.0, model.contamination(30L, Collections.<Integer, Long>emptyMap()), 0.0);
    }

    @Test
    public void readsSumsCountsAtOrAboveMinimumSupport() {
        Map<Integer, Long> altSupportMap = new HashMap<Integer, Long>();
        altSupportMap.put(0, 10L);
        altSupportMap.put(1, 20L);
        altSupportMap.put(2, 30L);
        altSupportMap.put(3, 40L);

        assertEquals(70L, TumorContaminationModel.reads(2, altSupportMap));
    }

    @Test
    public void readsReturnsZeroForEmptyMap() {
        assertEquals(0L, TumorContaminationModel.reads(2, Collections.<Integer, Long>emptyMap()));
    }
}
