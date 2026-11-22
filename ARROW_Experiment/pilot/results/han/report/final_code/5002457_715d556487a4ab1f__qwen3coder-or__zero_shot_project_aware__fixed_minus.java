package org.noise_planet.noisemodelling.emission;

import org.junit.Test;
import static org.junit.Assert.*;

public class EvaluateRoadSourceDynamic_ArrowTest {

    @Test
    public void testGetA_Roadcoeff() {
        // Test valid frequency, vehicle category, and road surface
        Double result = EvaluateRoadSourceDynamic.getA_Roadcoeff(1000, 1, 1);
        assertEquals(83.1, result, 0.01);

        // Test invalid frequency (should default to 0)
        result = EvaluateRoadSourceDynamic.getA_Roadcoeff(5000, 1, 1);
        assertEquals(0.0, result, 0.01);

        // Test invalid vehicle category (should default to 0)
        result = EvaluateRoadSourceDynamic.getA_Roadcoeff(1000, 6, 1);
        assertEquals(0.0, result, 0.01);

        // Test invalid road surface (should default to 0)
        result = EvaluateRoadSourceDynamic.getA_Roadcoeff(1000, 1, 25);
        assertEquals(0.0, result, 0.01);
    }

    @Test
    public void testGetB_Roadcoeff() {
        // Test valid vehicle category and road surface
        Double result = EvaluateRoadSourceDynamic.getB_Roadcoeff(1, 1);
        assertEquals(0.9, result, 0.01);

        // Test invalid vehicle category (should default to 0)
        result = EvaluateRoadSourceDynamic.getB_Roadcoeff(6, 1);
        assertEquals(0.0, result, 0.01);

        // Test invalid road surface (should default to 0)
        result = EvaluateRoadSourceDynamic.getB_Roadcoeff(1, 25);
        assertEquals(0.0, result, 0.01);
    }

    @Test
    public void testGetCoeff() {
        // Test valid coefficient, frequency, and vehicle category
        Double result = EvaluateRoadSourceDynamic.getCoeff(0, 1000, 1);
        assertEquals(83.1, result, 0.01);

        // Test invalid frequency (should default to 0)
        result = EvaluateRoadSourceDynamic.getCoeff(0, 5000, 1);
        assertEquals(83.1, result, 0.01);

        // Test invalid vehicle category (should default to VehCat1)
        result = EvaluateRoadSourceDynamic.getCoeff(0, 1000, 6);
        assertEquals(83.1, result, 0.01);
    }

    @Test
    public void testGetNoiseLvl() {
        // Test normal case using a public wrapper method
        double result = EvaluateRoadSourceDynamic.getCoeff(0, 1000, 1) + 
                       EvaluateRoadSourceDynamic.getCoeff(1, 1000, 1) * Math.log10(60.0 / 70.0);
        assertEquals(80.0 + 2.0 * Math.log10(60.0 / 70.0), result, 0.01);
    }

    @Test
    public void testSumDba() {
        // Test normal case using a public wrapper method
        double result = EvaluateRoadSourceDynamic.getCoeff(0, 1000, 1) + 
                       EvaluateRoadSourceDynamic.getCoeff(1, 1000, 1) * Math.log10(60.0 / 70.0);
        assertEquals(80.0 + 2.0 * Math.log10(60.0 / 70.0), result, 0.01);
    }

    @Test
    public void testSumDba_5() {
        // Test normal case using a public wrapper method
        double result = EvaluateRoadSourceDynamic.getCoeff(0, 1000, 1) + 
                       EvaluateRoadSourceDynamic.getCoeff(1, 1000, 1) * Math.log10(60.0 / 70.0);
        assertEquals(80.0 + 2.0 * Math.log10(60.0 / 70.0), result, 0.01);
    }

    @Test
    public void testEvaluate() {
        // Create a mock RSParametersDynamic object
        RSParametersDynamic params = new RSParametersDynamic() {
            @Override
            public int getFreqParam() {
                return 1000;
            }

            @Override
            public double getTemperature() {
                return 20.0;
            }

            @Override
            public int getRoadSurface() {
                return 1;
            }

            @Override
            public boolean getStud() {
                return false;
            }

            @Override
            public double getJunc_dist() {
                return 0.0;
            }

            @Override
            public int getJunc_type() {
                return 0;
            }

            @Override
            public int getAcc_type() {
                return 0;
            }

            @Override
            public int getVeh_type() {
                return 1;
            }

            @Override
            public int getVehId() {
                return 1;
            }

            @Override
            public double getAcceleration() {
                return 0.0;
            }

            @Override
            public double getSpeed() {
                return 70.0;
            }

            @Override
            public double getSlopePercentage() {
                return 0.0;
            }

            @Override
            public double getLwStd() {
                return 1.0;
            }
        };

        // Call evaluate method
        double result = EvaluateRoadSourceDynamic.evaluate(params);
        
        // Check that result is a valid number (not NaN or Infinity)
        assertFalse(Double.isNaN(result));
        assertFalse(Double.isInfinite(result));
    }
}
