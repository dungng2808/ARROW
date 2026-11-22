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
    public void testGetRoadSpeedMin() {
        // Test valid road surface
        Double result = EvaluateRoadSourceDynamic.getRoadSpeedMin(1);
        assertEquals(50.0, result, 0.01);

        // Test invalid road surface (should default to 20)
        result = EvaluateRoadSourceDynamic.getRoadSpeedMin(25);
        assertEquals(20.0, result, 0.01);
    }

    @Test
    public void testGetRoadSpeedMax() {
        // Test valid road surface
        Double result = EvaluateRoadSourceDynamic.getRoadSpeedMax(1);
        assertEquals(130.0, result, 0.01);

        // Test invalid road surface (should default to 130)
        result = EvaluateRoadSourceDynamic.getRoadSpeedMax(25);
        assertEquals(130.0, result, 0.01);
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
        // Test normal case
        Double result = EvaluateRoadSourceDynamic.getNoiseLvl(80.0, 2.0, 60.0, 70.0);
        assertEquals(80.0 + 2.0 * Math.log10(60.0 / 70.0), result, 0.01);
    }

    @Test
    public void testSumDba() {
        // Test normal case
        Double result = EvaluateRoadSourceDynamic.sumDba(60.0, 60.0);
        // Should be around 63.01 dB(A) since 2*10^(60/10) = 2*10^6 = 2000000 W
        // and 10*log10(2000000) = 10*log10(2) + 60 = 63.01
        assertEquals(63.01, result, 0.01);
    }

    @Test
    public void testSumDba_5() {
        // Test normal case
        Double result = EvaluateRoadSourceDynamic.sumDba_5(60.0, 60.0, 60.0, 60.0, 60.0);
        // Should be around 69.54 dB(A) since 5*10^(60/10) = 5*10^6 = 5000000 W
        // and 10*log10(5000000) = 10*log10(5) + 60 = 66.99
        assertEquals(66.99, result, 0.01);
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
