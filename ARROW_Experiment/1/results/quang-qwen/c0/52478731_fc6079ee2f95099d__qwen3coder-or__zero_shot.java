package ubic.gemma.core.loader.expression.geo;

import org.junit.Test;
import ubic.gemma.model.common.quantitationtype.*;
import static org.junit.Assert.*;

public class QuantitationTypeParameterGuesser_ArrowTest {

    @Test
    public void testGuessQuantitationTypeParameters_NameAndDescription() {
        QuantitationType qt = new QuantitationType();
        qt.setName("signal");
        qt.setDescription("This is a signal measurement");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "This is a signal measurement");

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
    public void testGuessQuantitationTypeParameters_PresentAbsentType() {
        QuantitationType qt = new QuantitationType();
        qt.setName("present_call");
        qt.setDescription("Detection call");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "present_call", "Detection call");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
        assertEquals(ScaleType.OTHER, qt.getScale());
        assertEquals(StandardQuantitationType.PRESENTABSENT, qt.getType());
        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_CoordinateType() {
        QuantitationType qt = new QuantitationType();
        qt.setName("x_coord");
        qt.setDescription("X coordinate");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "x_coord", "X coordinate");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.COORDINATE, qt.getType());
        assertEquals(PrimitiveType.INT, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_RatioType() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ratio");
        qt.setDescription("Fold change ratio");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ratio", "Fold change ratio");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertTrue(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_BackgroundSubtracted() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1_background_subtracted");
        qt.setDescription("Channel 1 background subtracted signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1_background_subtracted", "Channel 1 background subtracted signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertTrue(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_Normalized() {
        QuantitationType qt = new QuantitationType();
        qt.setName("normalized_signal");
        qt.setDescription("Normalized signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "normalized_signal", "Normalized signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertTrue(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_Preferred() {
        QuantitationType qt = new QuantitationType();
        qt.setName("rma");
        qt.setDescription("RMA signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "rma", "RMA signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertTrue(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_ScaleTypeLog2() {
        QuantitationType qt = new QuantitationType();
        qt.setName("log2_signal");
        qt.setDescription("Log2 transformed signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "log2_signal", "Log2 transformed signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LOG2, qt.getScale());
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
    public void testGuessQuantitationTypeParameters_PercentScale() {
        QuantitationType qt = new QuantitationType();
        qt.setName("percent_signal");
        qt.setDescription("Percentage signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "percent_signal", "Percentage signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.PERCENT, qt.getScale());
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
    public void testGuessQuantitationTypeParameters_ExampleValueInteger() {
        QuantitationType qt = new QuantitationType();
        qt.setName("count");
        qt.setDescription("Count of features");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "count", "Count of features", 10);

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.INT, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_ExampleValueString() {
        QuantitationType qt = new QuantitationType();
        qt.setName("flag");
        qt.setDescription("Flag information");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "flag", "Flag information", "valid");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_ExampleValueNull() {
        QuantitationType qt = new QuantitationType();
        qt.setName("signal");
        qt.setDescription("Signal measurement");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "Signal measurement", null);

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
    public void testGuessQuantitationTypeParameters_NullDescription() {
        QuantitationType qt = new QuantitationType();
        qt.setName("signal");
        qt.setDescription(null);

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", null);

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
    public void testGuessQuantitationTypeParameters_SpecialCaseProbeID() {
        QuantitationType qt = new QuantitationType();
        qt.setName("Probe ID");
        qt.setDescription("Probe Set ID");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "Probe ID", "Probe Set ID");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
        assertEquals(ScaleType.UNSCALED, qt.getScale());
        assertEquals(StandardQuantitationType.OTHER, qt.getType());
        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_QualitativeDescription() {
        QuantitationType qt = new QuantitationType();
        qt.setName("category");
        qt.setDescription("qualitative");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "category", "qualitative");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
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
    public void testGuessQuantitationTypeParameters_ExistingValuesNotOverwritten() {
        QuantitationType qt = new QuantitationType();
        qt.setName("test");
        qt.setDescription("test");
        qt.setGeneralType(GeneralType.CATEGORICAL);
        qt.setScale(ScaleType.LOG2);
        qt.setType(StandardQuantitationType.PRESENTABSENT);
        qt.setRepresentation(PrimitiveType.STRING);
        qt.setIsBackground(Boolean.TRUE);
        qt.setIsBackgroundSubtracted(Boolean.TRUE);
        qt.setIsNormalized(Boolean.TRUE);
        qt.setIsRatio(Boolean.TRUE);
        qt.setIsPreferred(Boolean.TRUE);
        qt.setIsMaskedPreferred(Boolean.TRUE);

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "test", "test");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
        assertEquals(ScaleType.LOG2, qt.getScale());
        assertEquals(StandardQuantitationType.PRESENTABSENT, qt.getType());
        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
        assertTrue(qt.getIsBackground());
        assertTrue(qt.getIsBackgroundSubtracted());
        assertTrue(qt.getIsNormalized());
        assertTrue(qt.getIsRatio());
        assertTrue(qt.getIsPreferred());
        assertTrue(qt.getIsMaskedPreferred());
    }
}
