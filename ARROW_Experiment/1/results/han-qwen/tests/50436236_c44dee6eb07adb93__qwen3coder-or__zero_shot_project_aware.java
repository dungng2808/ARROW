package com.hartwig.hmftools.common.amber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class TumorContaminationModel_ArrowTest {

    @Test
    public void testContaminationWithInsufficientThreePlusReads() {
        TumorContaminationModel model = new TumorContaminationModel();
        List<TumorContamination> unfiltered = new ArrayList<>();
        // Add entries with less than MIN_THREE_PLUS_READS
        Map<Integer, Long> altSupportMap = new HashMap<>();
        altSupportMap.put(0, 1000L);
        altSupportMap.put(1, 1000L);
        altSupportMap.put(2, 500L);
        altSupportMap.put(3, 100L);

        double result = model.contamination(1000L, altSupportMap);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testContaminationWithSufficientThreePlusReads() {
        TumorContaminationModel model = new TumorContaminationModel();
        Map<Integer, Long> altSupportMap = new HashMap<>();
        altSupportMap.put(0, 1000L);
        altSupportMap.put(1, 1000L);
        altSupportMap.put(2, 500L);
        altSupportMap.put(3, 1500L); // Total 2000+ three-plus reads

        double result = model.contamination(1000L, altSupportMap);
        assertTrue(result >= 0.0 && result <= 1.0);
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
        List<TumorContamination> contamination = new ArrayList<>();
        // Mock TumorContamination objects with read depths
        contamination.add(new TumorContamination() {
            @Override
            public TumorContamination.Tumor tumor() {
                return new TumorContamination.Tumor() {
                    @Override
                    public int readDepth() {
                        return 100;
                    }

                    @Override
                    public int altSupport() {
                        return 0;
                    }
                };
            }

            @Override
            public TumorContamination.Tumor normal() {
                return new TumorContamination.Tumor() {
                    @Override
                    public int readDepth() {
                        return 10;
                    }

                    @Override
                    public int altSupport() {
                        return 0;
                    }
                };
            }
        });

        contamination.add(new TumorContamination() {
            @Override
            public TumorContamination.Tumor tumor() {
                return new TumorContamination.Tumor() {
                    @Override
                    public int readDepth() {
                        return 200;
                    }

                    @Override
                    public int altSupport() {
                        return 0;
                    }
                };
            }

            @Override
            public TumorContamination.Tumor normal() {
                return new TumorContamination.Tumor() {
                    @Override
                    public int readDepth() {
                        return 10;
                    }

                    @Override
                    public int altSupport() {
                        return 0;
                    }
                };
            }
        });

        int result = TumorContaminationModel.medianDepth(contamination);
        assertEquals(150, result);
    }

    @Test
    public void testContaminationScore() {
        Map<Integer, Long> altSupportMap = new HashMap<>();
        altSupportMap.put(0, 1000L);
        altSupportMap.put(1, 1000L);
        altSupportMap.put(2, 500L);
        altSupportMap.put(3, 1500L);

        TumorContaminationModel model = new TumorContaminationModel();
        double result = model.contaminationScore(0.1, 2000L, 1000L, altSupportMap);
        assertTrue(result >= 0.0);
    }
}
