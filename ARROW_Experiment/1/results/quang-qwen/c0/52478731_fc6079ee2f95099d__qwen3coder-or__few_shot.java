package ubic.gemma.core.loader.expression.geo;

import org.junit.Test;
import ubic.gemma.model.common.quantitationtype.*;
import static org.junit.Assert.*;

public class QuantitationTypeParameterGuesser_ArrowTest {

    @Test
    public void testGuessQuantitationTypeParameters_NameAndDescription() {
        QuantitationType qt = new QuantitationType();
        qt.setName("signal");
        qt.setDescription("raw signal intensity");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "raw signal intensity");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsBackgroundTrue() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1b");
        qt.setDescription("background intensity");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1b", "background intensity");

        assertTrue(qt.getIsBackground());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsBackgroundSubtractedTrue() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1d");
        qt.setDescription("background subtracted signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1d", "background subtracted signal");

        assertTrue(qt.getIsBackgroundSubtracted());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsNormalizedTrue() {
        QuantitationType qt = new QuantitationType();
        qt.setName("rma");
        qt.setDescription("RMA normalized signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "rma", "RMA normalized signal");

        assertTrue(qt.getIsNormalized());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsRatioTrue() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ratio");
        qt.setDescription("fold change ratio");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ratio", "fold change ratio");

        assertTrue(qt.getIsRatio());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsPreferredTrue() {
        QuantitationType qt = new QuantitationType();
        qt.setName("rma");
        qt.setDescription("RMA signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "rma", "RMA signal");

        assertTrue(qt.getIsPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_PresentAbsentType() {
        QuantitationType qt = new QuantitationType();
        qt.setName("present_call");
        qt.setDescription("detection call");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "present_call", "detection call");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
        assertEquals(ScaleType.OTHER, qt.getScale());
        assertEquals(StandardQuantitationType.PRESENTABSENT, qt.getType());
    }

    @Test
    public void testGuessQuantitationTypeParameters_CoordinateType() {
        QuantitationType qt = new QuantitationType();
        qt.setName("x_coord");
        qt.setDescription("x coordinate");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "x_coord", "x coordinate");

        assertEquals(PrimitiveType.INT, qt.getRepresentation());
    }

    @Test
    public void testGuessQuantitationTypeParameters_OtherType() {
        QuantitationType qt = new QuantitationType();
        qt.setName("Probe ID");
        qt.setDescription("Probe Set ID");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "Probe ID", "Probe Set ID");

        assertEquals(StandardQuantitationType.OTHER, qt.getType());
        assertEquals(ScaleType.UNSCALED, qt.getScale());
        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
    }

    @Test
    public void testGuessQuantitationTypeParameters_CategoricalDescription() {
        QuantitationType qt = new QuantitationType();
        qt.setName("category");
        qt.setDescription("qualitative description");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "category", "qualitative description");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
    }

    @Test
    public void testGuessQuantitationTypeParameters_ExampleValueInteger() {
        QuantitationType qt = new QuantitationType();
        qt.setName("count");
        qt.setDescription("number of features");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "count", "number of features", 42);

        assertEquals(PrimitiveType.INT, qt.getRepresentation());
    }

    @Test
    public void testGuessQuantitationTypeParameters_ExampleValueDouble() {
        QuantitationType qt = new QuantitationType();
        qt.setName("intensity");
        qt.setDescription("signal intensity");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "intensity", "signal intensity", 42.5);

        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
    }

    @Test
    public void testGuessQuantitationTypeParameters_ExampleValueString() {
        QuantitationType qt = new QuantitationType();
        qt.setName("flag");
        qt.setDescription("quality flag");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "flag", "quality flag", "good");

        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
    }

    @Test
    public void testGuessQuantitationTypeParameters_ExampleValueNull() {
        QuantitationType qt = new QuantitationType();
        qt.setName("value");
        qt.setDescription("raw value");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "value", "raw value", null);

        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
    }

    @Test
    public void testGuessQuantitationTypeParameters_ExampleValueStringNull() {
        QuantitationType qt = new QuantitationType();
        qt.setName("value");
        qt.setDescription("raw value");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "value", "raw value", "null");

        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
    }

    @Test
    public void testGuessQuantitationTypeParameters_ScaleTypeLog2() {
        QuantitationType qt = new QuantitationType();
        qt.setName("log2_signal");
        qt.setDescription("log2 transformed signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "log2_signal", "log2 transformed signal");

        assertEquals(ScaleType.LOG2, qt.getScale());
    }

    @Test
    public void testGuessQuantitationTypeParameters_ScaleTypePercent() {
        QuantitationType qt = new QuantitationType();
        qt.setName("%_signal");
        qt.setDescription("percentage signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "%_signal", "percentage signal");

        assertEquals(ScaleType.PERCENT, qt.getScale());
    }

    @Test
    public void testGuessQuantitationTypeParameters_TypeAmount() {
        QuantitationType qt = new QuantitationType();
        qt.setName("signal_intensity");
        qt.setDescription("raw signal intensity");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal_intensity", "raw signal intensity");

        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
    }

    @Test
    public void testGuessQuantitationTypeParameters_TypeConfidenceIndicator() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1_sd");
        qt.setDescription("channel 1 standard deviation");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1_sd", "channel 1 standard deviation");

        assertEquals(StandardQuantitationType.CONFIDENCEINDICATOR, qt.getType());
    }

    @Test
    public void testGuessQuantitationTypeParameters_TypeCoordinate() {
        QuantitationType qt = new QuantitationType();
        qt.setName("array_row");
        qt.setDescription("array row position");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "array_row", "array row position");

        assertEquals(StandardQuantitationType.COORDINATE, qt.getType());
    }

    @Test
    public void testGuessQuantitationTypeParameters_TypeCorrelation() {
        QuantitationType qt = new QuantitationType();
        qt.setName("correlation");
        qt.setDescription("correlation coefficient");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "correlation", "correlation coefficient");

        assertEquals(StandardQuantitationType.CORRELATION, qt.getType());
    }

    @Test
    public void testGuessQuantitationTypeParameters_TypeOther() {
        QuantitationType qt = new QuantitationType();
        qt.setName("area");
        qt.setDescription("area measurement");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "area", "area measurement");

        assertEquals(StandardQuantitationType.OTHER, qt.getType());
    }

    @Test
    public void testGuessQuantitationTypeParameters_DefaultValues() {
        QuantitationType qt = new QuantitationType();
        qt.setName("unknown");
        qt.setDescription("unknown type");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "unknown type");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }
}
