package org.noise_planet.noisemodelling.emission;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EvaluateRoadSourceDynamic_ArrowTest {

    @Test
    public void roadCoefficientForKnownFrequencyVehicleAndSurface() {
        assertEquals(5.4, EvaluateRoadSourceDynamic.getA_Roadcoeff(125, 1, 1), 0.0);
    }

    @Test
    public void roadCoefficientDefaultsToFirstFrequencyForUnknownFrequency() {
        assertEquals(0.0, EvaluateRoadSourceDynamic.getA_Roadcoeff(999, 1, 1), 0.0);
    }

    @Test
    public void roadCoefficientDefaultsToZeroForUnknownSurface() {
        assertEquals(0.0, EvaluateRoadSourceDynamic.getA_Roadcoeff(63, 1, 99), 0.0);
    }

    @Test
    public void bRoadCoefficientForKnownSurface() {
        assertEquals(-6.5, EvaluateRoadSourceDynamic.getB_Roadcoeff(1, 1), 0.0);
    }

    @Test
    public void bRoadCoefficientDefaultsToZeroForUnknownSurface() {
        assertEquals(0.0, EvaluateRoadSourceDynamic.getB_Roadcoeff(1, 99), 0.0);
    }

    @Test
    public void vehicleCoefficientForKnownCategoryAndFrequency() {
        assertEquals(89.2, EvaluateRoadSourceDynamic.getCoeff(0, 125, 1), 0.0);
    }

    @Test
    public void vehicleCoefficientUsesFirstFrequencyForUnknownFrequency() {
        assertEquals(83.1, EvaluateRoadSourceDynamic.getCoeff(0, 999, 1), 0.0);
    }

    @Test
    public void vehicleCoefficientDefaultsToPassengerCarForUnknownCategory() {
        assertEquals(97.9, EvaluateRoadSourceDynamic.getCoeff(2, 63, 999), 0.0);
    }

    @Test
    public void evaluateReturnsFiniteNoiseLevelForValidParameters() {
        RSParametersDynamic parameters = new RSParametersDynamic();
        parameters.setFreqParam(500);
        parameters.setTemperature(20.0);
        parameters.setRoadSurface(1);
        parameters.setStud(false);
        parameters.setJunc_dist(0.0);
        parameters.setJunc_type(1);
        parameters.setAcc_type(0);
        parameters.setVeh_type(1);
        parameters.setVehId(7);
        parameters.setAcceleration(0.0);
        parameters.setSpeed(70.0);
        parameters.setSlopePercentage(0.0);
        parameters.setLwStd(0.0);

        double result = EvaluateRoadSourceDynamic.evaluate(parameters);

        assertTrue(Double.isFinite(result));
    }
}
