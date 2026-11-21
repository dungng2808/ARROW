package com.hartwig.hmftools.common.amber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TumorContaminationModel_ArrowTest {

    @Test
    public void testContaminationWithNoEvidence() {
        TumorContaminationModel model = new TumorContaminationModel();
        List<TumorContamination> contaminationList = new ArrayList<>();
        
        // Add a contamination entry with low read depth
        TumorContamination cont = new TumorContamination() {
            @Override
            public TumorContamination.Normal normal() {
                return new TumorContamination.Normal() {
                    @Override
                    public int readDepth() {
                        return 5; // Below MIN_NORMAL_READ_DEPTH
                    }
                };
            }
            
            @Override
            public TumorContamination.Tumor tumor() {
                return new TumorContamination.Tumor() {
                    @Override
                    public int altSupport() {
                        return 3;
                    }
                    
                    @Override
                    public int readDepth() {
                        return 100;
                    }
                };
            }
        };
        
        contaminationList.add(cont);
        
        double result = model.contamination(contaminationList);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testContaminationWithEvidence() {
        TumorContaminationModel model = new TumorContaminationModel();
        List<TumorContamination> contaminationList = new ArrayList<>();
        
        // Add contamination entries with sufficient read depth
        TumorContamination cont1 = new TumorContamination() {
            @Override
            public TumorContamination.Normal normal() {
                return new TumorContamination.Normal() {
                    @Override
                    public int readDepth() {
                        return 10;
                    }
                };
            }
            
            @Override
            public TumorContamination.Tumor tumor() {
                return new TumorContamination.Tumor() {
                    @Override
                    public int altSupport() {
                        return 3;
                    }
                    
                    @Override
                    public int readDepth() {
                        return 100;
                    }
                };
            }
        };
        
        TumorContamination cont2 = new TumorContamination() {
            @Override
            public TumorContamination.Normal normal() {
                return new TumorContamination.Normal() {
                    @Override
                    public int readDepth() {
                        return 15;
                    }
                };
            }
            
            @Override
            public TumorContamination.Tumor tumor() {
                return new TumorContamination.Tumor() {
                    @Override
                    public int altSupport() {
                        return 4;
                    }
                    
                    @Override
                    public int readDepth() {
                        return 120;
                    }
                };
            }
        };
        
        contaminationList.add(cont1);
        contaminationList.add(cont2);
        
        double result = model.contamination(contaminationList);
        // Should return 0 since we don't have enough data to determine contamination
        assertEquals(0.0, result, 0.001);
    }

    @Test
    public void testContaminationScore() {
        TumorContaminationModel model = new TumorContaminationModel();
        Map<Integer, Long> altSupportMap = new HashMap<>();
        altSupportMap.put(0, 100L);
        altSupportMap.put(1, 200L);
        altSupportMap.put(2, 300L);
        altSupportMap.put(3, 400L);
        
        double score = model.contaminationScore(0.1, 700L, 100L, altSupportMap);
        assertTrue(score > 0);
    }

    @Test
    public void testReads() {
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
        List<TumorContamination> contaminationList = new ArrayList<>();
        
        TumorContamination cont1 = new TumorContamination() {
            @Override
            public TumorContamination.Normal normal() {
                return new TumorContamination.Normal() {
                    @Override
                    public int readDepth() {
                        return 10;
                    }
                };
            }
            
            @Override
            public TumorContamination.Tumor tumor() {
                return new TumorContamination.Tumor() {
                    @Override
                    public int altSupport() {
                        return 3;
                    }
                    
                    @Override
                    public int readDepth() {
                        return 100;
                    }
                };
            }
        };
        
        TumorContamination cont2 = new TumorContamination() {
            @Override
            public TumorContamination.Normal normal() {
                return new TumorContamination.Normal() {
                    @Override
                    public int readDepth() {
                        return 15;
                    }
                };
            }
            
            @Override
            public TumorContamination.Tumor tumor() {
                return new TumorContamination.Tumor() {
                    @Override
                    public int altSupport() {
                        return 4;
                    }
                    
                    @Override
                    public int readDepth() {
                        return 120;
                    }
                };
            }
        };
        
        contaminationList.add(cont1);
        contaminationList.add(cont2);
        
        int result = TumorContaminationModel.medianDepth(contaminationList);
        assertEquals(100, result);
    }
}
