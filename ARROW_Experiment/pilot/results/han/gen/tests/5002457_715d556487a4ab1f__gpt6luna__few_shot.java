package org.noise_planet.noisemodelling.emission;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EvaluateRoadSourceDynamic_ArrowTest {

    @Test
    public void roadCoefficientReturnsKnownValue() {
        assertEquals(Double.valueOf(5.4),
                EvaluateRoadSourceDynamic.getA_Roadcoeff(125, 1, 1));
    }

    @Test
    public void roadCoefficientUsesFirstFrequencyForUnknownFrequency() {
        assertEquals(Double.valueOf(0.0),
                EvaluateRoadSourceDynamic.getA_Roadcoeff(999, 1, 1));
    }

    @Test
    public void roadCoefficientReturnsZeroForUnknownSurface() {
        assertEquals(Double.valueOf(0.0),
                EvaluateRoadSourceDynamic.getA_Roadcoeff(63, 1, 99));
    }

    @Test
    public void bRoadCoefficientReturnsKnownValue() {
        assertEquals(Double.valueOf(-6.5),
                EvaluateRoadSourceDynamic.getB_Roadcoeff(1, 1));
    }

    @Test
    public void bRoadCoefficientReturnsZeroForUnknownSurface() {
        assertEquals(Double.valueOf(0.0),
                EvaluateRoadSourceDynamic.getB_Roadcoeff(1, 99));
    }

    @Test
    public void vehicleCoefficientReturnsKnownValue() {
        assertEquals(Double.valueOf(89.2),
                EvaluateRoadSourceDynamic.getCoeff(0, 125, 1));
    }

    @Test
    public void vehicleCoefficientUsesFirstFrequencyForUnknownFrequency() {
        assertEquals(Double.valueOf(83.1),
                EvaluateRoadSourceDynamic.getCoeff(0, 999, 1));
    }

    @Test
    public void unknownVehicleCategoryUsesLightVehicleCoefficients() {
        assertEquals(Double.valueOf(83.1),
                EvaluateRoadSourceDynamic.getCoeff(0, 63, 99));
    }

    @Test
    public void vehicleCoefficientRejectsInvalidCoefficientIndex() {
        try {
            EvaluateRoadSourceDynamic.getCoeff(6, 63, 1);
        } catch (ArrayIndexOutOfBoundsException expected) {
            return;
        }
        throw new AssertionError("Expected ArrayIndexOutOfBoundsException");
    }
}
