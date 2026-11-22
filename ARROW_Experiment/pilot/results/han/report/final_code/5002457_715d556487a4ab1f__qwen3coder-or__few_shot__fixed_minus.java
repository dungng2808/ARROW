package org.noise_planet.noisemodelling.emission;

import org.junit.Test;
import static org.junit.Assert.*;

public class EvaluateRoadSourceDynamic_ArrowTest {

    @Test
    public void testGetA_Roadcoeff() {
        // Test with valid parameters
        Double result = EvaluateRoadSourceDynamic.getA_Roadcoeff(1000, 1, 1);
        assertEquals(5.4, result, 0.0);
        
        // Test with invalid frequency (should default to 0)
        result = EvaluateRoadSourceDynamic.getA_Roadcoeff(50, 1, 1);
        assertEquals(0.0, result, 0.0);
        
        // Test with invalid vehicle category (should default to 0)
        result = EvaluateRoadSourceDynamic.getA_Roadcoeff(1000, 99, 1);
        assertEquals(0.0, result, 0.0);
        
        // Test with invalid road surface (should default to 0)
        result = EvaluateRoadSourceDynamic.getA_Roadcoeff(1000, 1, 99);
        assertEquals(0.0, result, 0.0);
    }

    @Test
    public void testGetB_Roadcoeff() {
        // Test with valid parameters
        Double result = EvaluateRoadSourceDynamic.getB_Roadcoeff(1, 1);
        assertEquals(0.9, result, 0.0);
        
        // Test with invalid vehicle category (should default to 0)
        result = EvaluateRoadSourceDynamic.getB_Roadcoeff(99, 1);
        assertEquals(0.0, result, 0.0);
        
        // Test with invalid road surface (should default to 0)
        result = EvaluateRoadSourceDynamic.getB_Roadcoeff(1, 99);
        assertEquals(0.0, result, 0.0);
    }

    @Test
    public void testGetCoeff() {
        // Test with valid parameters (Ar coefficient)
        Double result = EvaluateRoadSourceDynamic.getCoeff(0, 1000, 1);
        assertEquals(89.2, result, 0.0);
        
        // Test with invalid frequency (should default to 0)
        result = EvaluateRoadSourceDynamic.getCoeff(0, 50, 1);
        assertEquals(83.1, result, 0.0);
        
        // Test with invalid vehicle category (should default to VehCat1)
        result = EvaluateRoadSourceDynamic.getCoeff(0, 1000, 99);
        assertEquals(83.1, result, 0.0);
    }

    @Test
    public void testGetNoiseLvl() {
        // Test with valid parameters
        double result = EvaluateRoadSourceDynamic.getNoiseLvl(80.0, 2.0, 70.0, 70.0);
        assertEquals(80.0, result, 0.0);
        
        // Test with different speed
        result = EvaluateRoadSourceDynamic.getNoiseLvl(80.0, 2.0, 140.0, 70.0);
        assertEquals(80.0 + 2.0 * Math.log10(140.0 / 70.0), result, 0.0001);
    }

    @Test
    public void testEvaluate() {
        // Create a test parameters object using the constructor that takes parameters
        RSParametersDynamic params = new RSParametersDynamic(1000, 20.0, 1, false, 0.0, 0, 0, 1, 1, 0.0, 70.0, 0.0, 1.0);
        
        // Test the evaluate method
        double result = EvaluateRoadSourceDynamic.evaluate(params);
        assertTrue(result > 0);
    }
}
