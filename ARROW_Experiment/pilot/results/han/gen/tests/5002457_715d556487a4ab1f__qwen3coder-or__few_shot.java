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
    public void testGetRoadSpeedMin() {
        // Test with valid road surface
        Double result = EvaluateRoadSourceDynamic.getRoadSpeedMin(1);
        assertEquals(50.0, result, 0.0);
        
        // Test with invalid road surface (should default to 20)
        result = EvaluateRoadSourceDynamic.getRoadSpeedMin(99);
        assertEquals(20.0, result, 0.0);
    }

    @Test
    public void testGetRoadSpeedMax() {
        // Test with valid road surface
        Double result = EvaluateRoadSourceDynamic.getRoadSpeedMax(1);
        assertEquals(130.0, result, 0.0);
        
        // Test with invalid road surface (should default to 130)
        result = EvaluateRoadSourceDynamic.getRoadSpeedMax(99);
        assertEquals(130.0, result, 0.0);
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
        Double result = EvaluateRoadSourceDynamic.getNoiseLvl(80.0, 2.0, 70.0, 70.0);
        assertEquals(80.0, result, 0.0);
        
        // Test with different speed
        result = EvaluateRoadSourceDynamic.getNoiseLvl(80.0, 2.0, 140.0, 70.0);
        assertEquals(80.0 + 2.0 * Math.log10(140.0 / 70.0), result, 0.0001);
    }

    @Test
    public void testEvaluate() {
        // Create a test parameters object
        RSParametersDynamic params = new RSParametersDynamic();
        params.setFreqParam(1000);
        params.setTemperature(20.0);
        params.setRoadSurface(1);
        params.setStud(false);
        params.setJunc_dist(0.0);
        params.setJunc_type(0);
        params.setAcc_type(0);
        params.setVeh_type(1);
        params.setVehId(1);
        params.setAcceleration(0.0);
        params.setSpeed(70.0);
        params.setSlopePercentage(0.0);
        params.setLwStd(1.0);
        
        // Test the evaluate method
        double result = EvaluateRoadSourceDynamic.evaluate(params);
        assertTrue(result > 0);
    }
}
