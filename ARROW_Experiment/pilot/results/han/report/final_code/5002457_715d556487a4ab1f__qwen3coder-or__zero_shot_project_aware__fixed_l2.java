package org.noise_planet.noisemodelling.emission;

import org.junit.Test;
import static org.junit.Assert.*;

public class EvaluateRoadSourceDynamic_ArrowTest {

    @Test
    public void testGetA_Roadcoeff() {
        // Test valid frequency, vehicle category, and road surface
        Double result = EvaluateRoadSourceDynamic.getA_Roadcoeff(1000, 1, 1);
        assertEquals(-1.0, result, 0.01);

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
        assertEquals(-6.5, result, 0.01);

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
        assertEquals(100.1, result, 0.01);

        // Test invalid frequency (should default to 0)
        result = EvaluateRoadSourceDynamic.getCoeff(0, 5000, 1);
        assertEquals(100.1, result, 0.01);

        // Test invalid vehicle category (should default to VehCat1)
        result = EvaluateRoadSourceDynamic.getCoeff(0, 1000, 6);
        assertEquals(100.1, result, 0.01);
    }

    @Test
    public void testEvaluate() {
        // Create a mock RSParametersDynamic object
        RSParametersDynamic params = new RSParametersDynamic(
                70.0, // speed
                0.0,  // acceleration
                1,    // veh_type
                0,    // acc_type
                1000, // FreqParam
                20.0, // Temperature
                1,    // RoadSurface
                false,// Stud
                0.0,  // Junc_dist
                0,    // Junc_type
                1.0,  // LwStd
                1     // VehId
        );

        // Call evaluate method
        double result = EvaluateRoadSourceDynamic.evaluate(params);
        
        // Check that result is a valid number (not NaN or Infinity)
        assertFalse(Double.isNaN(result));
        assertFalse(Double.isInfinite(result));
    }
}
