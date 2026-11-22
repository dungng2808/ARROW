package org.noise_planet.noisemodelling.emission;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EvaluateRoadSourceDynamic_ArrowTest {

    @Test
    public void getARoadcoeffReturnsCoefficientForFrequencyVehicleAndSurface() {
        assertEquals(5.4, EvaluateRoadSourceDynamic.getA_Roadcoeff(125, 1, 1), 0.0);
    }

    @Test
    public void getARoadcoeffUsesFirstFrequencyForUnknownFrequency() {
        assertEquals(0.0, EvaluateRoadSourceDynamic.getA_Roadcoeff(999, 1, 1), 0.0);
    }

    @Test
    public void getARoadcoeffUsesFirstVehicleCategoryForUnknownCategory() {
        assertEquals(5.4, EvaluateRoadSourceDynamic.getA_Roadcoeff(125, 999, 1), 0.0);
    }

    @Test
    public void getARoadcoeffReturnsZeroForUnknownSurface() {
        assertEquals(0.0, EvaluateRoadSourceDynamic.getA_Roadcoeff(125, 1, 999), 0.0);
    }

    @Test
    public void getBRoadcoeffReturnsCoefficientForVehicleAndSurface() {
        assertEquals(-6.5, EvaluateRoadSourceDynamic.getB_Roadcoeff(1, 1), 0.0);
    }

    @Test
    public void getBRoadcoeffUsesFirstVehicleCategoryForUnknownCategory() {
        assertEquals(-6.5, EvaluateRoadSourceDynamic.getB_Roadcoeff(999, 1), 0.0);
    }

    @Test
    public void getBRoadcoeffReturnsZeroForUnknownSurface() {
        assertEquals(0.0, EvaluateRoadSourceDynamic.getB_Roadcoeff(1, 999), 0.0);
    }

    @Test
    public void getCoeffReturnsVehicleCoefficientAtFrequency() {
        assertEquals(89.2, EvaluateRoadSourceDynamic.getCoeff(0, 125, 1), 0.0);
    }

    @Test
    public void getCoeffUsesFirstFrequencyForUnknownFrequency() {
        assertEquals(83.1, EvaluateRoadSourceDynamic.getCoeff(0, 999, 1), 0.0);
    }

    @Test
    public void getCoeffUsesPassengerCarCoefficientsForUnknownVehicleCategory() {
        assertEquals(83.1, EvaluateRoadSourceDynamic.getCoeff(0, 63, 999), 0.0);
    }

    @Test
    public void getCoeffReturnsRequestedCoefficientColumn() {
        assertEquals(-1.3, EvaluateRoadSourceDynamic.getCoeff(3, 63, 1), 0.0);
    }
}
