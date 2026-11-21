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
    public void testGuessQuantitationTypeParameters_PresentAbsentType() {
        QuantitationType qt = new QuantitationType();
        qt.setName("present_call");
        qt.setDescription("detection call");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "present_call", "detection call");

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
        qt.setDescription("x coordinate");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "x_coord", "x coordinate");

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
        qt.setDescription("fold change ratio");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ratio", "fold change ratio");

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
        qt.setDescription("background subtracted signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1_background_subtracted", "background subtracted signal");

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
        qt.setDescription("normalized signal intensity");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "normalized_signal", "normalized signal intensity");

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
    public void testGuessQuantitationTypeParameters_ScaleLog2() {
        QuantitationType qt = new QuantitationType();
        qt.setName("log2_signal");
        qt.setDescription("log2 transformed signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "log2_signal", "log2 transformed signal");

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
    public void testGuessQuantitationTypeParameters_ScalePercent() {
        QuantitationType qt = new QuantitationType();
        qt.setName("%_signal");
        qt.setDescription("percentage signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "%_signal", "percentage signal");

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
        qt.setDescription("number of features");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "count", "number of features", 10);

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
        qt.setDescription("quality flag");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "flag", "quality flag", "PASS");

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
        qt.setDescription("raw signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "raw signal", null);

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
        qt.setName("call");
        qt.setDescription("qualitative call");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "call", "qualitative call");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
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
    public void testGuessQuantitationTypeParameters_EmptyStrings() {
        QuantitationType qt = new QuantitationType();
        qt.setName("");
        qt.setDescription("");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "", "");

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
    public void testGuessQuantitationTypeParameters_BooleanExampleValue() {
        QuantitationType qt = new QuantitationType();
        qt.setName("flag");
        qt.setDescription("quality flag");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "flag", "quality flag", true);

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.BOOLEAN, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_DoubleExampleValue() {
        QuantitationType qt = new QuantitationType();
        qt.setName("intensity");
        qt.setDescription("signal intensity");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "intensity", "signal intensity", 10.5);

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
    public void testGuessQuantitationTypeParameters_StringExampleValueWithNull() {
        QuantitationType qt = new QuantitationType();
        qt.setName("flag");
        qt.setDescription("quality flag");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "flag", "quality flag", "null");

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
    public void testGuessQuantitationTypeParameters_StringExampleValueWithNumber() {
        QuantitationType qt = new QuantitationType();
        qt.setName("count");
        qt.setDescription("number of features");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "count", "number of features", "10");

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
    public void testGuessQuantitationTypeParameters_UnknownTypeDefault() {
        QuantitationType qt = new QuantitationType();
        qt.setName("unknown_type");
        qt.setDescription("some unknown type");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown_type", "some unknown type");

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
    public void testGuessQuantitationTypeParameters_BackgroundDetection() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1_background");
        qt.setDescription("background intensity");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1_background", "background intensity");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertTrue(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_BackgroundSubtractedDetection() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1_bkd");
        qt.setDescription("background subtracted signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1_bkd", "background subtracted signal");

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
    public void testGuessQuantitationTypeParameters_RatioDetection() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ratio");
        qt.setDescription("fold change ratio");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ratio", "fold change ratio");

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
    public void testGuessQuantitationTypeParameters_CountType() {
        QuantitationType qt = new QuantitationType();
        qt.setName("count");
        qt.setDescription("number of features");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "count", "number of features");

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
    public void testGuessQuantitationTypeParameters_ConfidenceIndicator() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1_sd");
        qt.setDescription("standard deviation");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1_sd", "standard deviation");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.CONFIDENCEINDICATOR, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_CorrelationType() {
        QuantitationType qt = new QuantitationType();
        qt.setName("correlation");
        qt.setDescription("correlation coefficient");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "correlation", "correlation coefficient");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.CORRELATION, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_OtherType() {
        QuantitationType qt = new QuantitationType();
        qt.setName("area");
        qt.setDescription("area measurement");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "area", "area measurement");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.OTHER, qt.getType());
        assertEquals(PrimitiveType.INT, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_LogBaseUnknown() {
        QuantitationType qt = new QuantitationType();
        qt.setName("log_signal");
        qt.setDescription("log transformed signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "log_signal", "log transformed signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LOGBASEUNKNOWN, qt.getScale());
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
    public void testGuessQuantitationTypeParameters_NaturalLog() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ln_signal");
        qt.setDescription("natural log signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ln_signal", "natural log signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LN, qt.getScale());
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
    public void testGuessQuantitationTypeParameters_MeasuredSignalNamePattern() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1_mean");
        qt.setDescription("channel 1 mean signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1_mean", "channel 1 mean signal");

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
    public void testGuessQuantitationTypeParameters_DerivedSignalNamePattern() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1d");
        qt.setDescription("derived signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1d", "derived signal");

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
    public void testGuessQuantitationTypeParameters_MeasuredSignalDescPattern() {
        QuantitationType qt = new QuantitationType();
        qt.setName("signal");
        qt.setDescription("channel 1 mean signal intensity");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "channel 1 mean signal intensity");

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
    public void testGuessQuantitationTypeParameters_DerivedSignalDescPattern() {
        QuantitationType qt = new QuantitationType();
        qt.setName("signal");
        qt.setDescription("background subtracted signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "background subtracted signal");

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
    public void testGuessQuantitationTypeParameters_ScaleNamePattern() {
        QuantitationType qt = new QuantitationType();
        qt.setName("%_signal");
        qt.setDescription("percentage signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "%_signal", "percentage signal");

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
    public void testGuessQuantitationTypeParameters_TypeNamePattern() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1_sd");
        qt.setDescription("standard deviation");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1_sd", "standard deviation");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.CONFIDENCEINDICATOR, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_TypeDescPattern() {
        QuantitationType qt = new QuantitationType();
        qt.setName("call");
        qt.setDescription("detection call");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "call", "detection call");

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
    public void testGuessQuantitationTypeParameters_RepresentationDescPattern() {
        QuantitationType qt = new QuantitationType();
        qt.setName("area");
        qt.setDescription("area measurement");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "area", "area measurement");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.OTHER, qt.getType());
        assertEquals(PrimitiveType.INT, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsBackgroundDescPattern() {
        QuantitationType qt = new QuantitationType();
        qt.setName("background");
        qt.setDescription("background intensity");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "background", "background intensity");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertTrue(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsRatioDescPattern() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ratio");
        qt.setDescription("fold change ratio");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ratio", "fold change ratio");

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
    public void testGuessQuantitationTypeParameters_IsNormalizedPattern() {
        QuantitationType qt = new QuantitationType();
        qt.setName("normalized");
        qt.setDescription("normalized signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "normalized", "normalized signal");

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
    public void testGuessQuantitationTypeParameters_IsBackgroundSubtractedPattern() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1d");
        qt.setDescription("background subtracted signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1d", "background subtracted signal");

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
    public void testGuessQuantitationTypeParameters_IsPreferredPattern() {
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
    public void testGuessQuantitationTypeParameters_MaybeBackgroundFalse() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1_b");
        qt.setDescription("background over the background");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1_b", "background over the background");

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
    public void testGuessQuantitationTypeParameters_MaybeDerivedSignalFalse() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ratio");
        qt.setDescription("ratio signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ratio", "ratio signal");

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
    public void testGuessQuantitationTypeParameters_MaybeMeasuredSignalFalse() {
        QuantitationType qt = new QuantitationType();
        qt.setName("sd");
        qt.setDescription("standard deviation");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "sd", "standard deviation");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.CONFIDENCEINDICATOR, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_MaybeRatioFalse() {
        QuantitationType qt = new QuantitationType();
        qt.setName("pm_excess");
        qt.setDescription("pm excess");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "pm_excess", "pm excess");

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
    public void testGuessQuantitationTypeParameters_CheckTypeDouble() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ratio");
        qt.setDescription("ratio signal");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ratio", "ratio signal");

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
    public void testGuessQuantitationTypeParameters_CheckTypeInt() {
        QuantitationType qt = new QuantitationType();
        qt.setName("count");
        qt.setDescription("number of features");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "count", "number of features");

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
    public void testGuessQuantitationTypeParameters_CheckTypeString() {
        QuantitationType qt = new QuantitationType();
        qt.setName("flag");
        qt.setDescription("quality flag");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "flag", "quality flag");

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
    public void testGuessQuantitationTypeParameters_CheckTypeBoolean() {
        QuantitationType qt = new QuantitationType();
        qt.setName("flag");
        qt.setDescription("quality flag");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "flag", "quality flag", true);

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.BOOLEAN, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_CheckTypeNull() {
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

    @Test
    public void testGuessQuantitationTypeParameters_GeneralTypeOverride() {
        QuantitationType qt = new QuantitationType();
        qt.setName("present_call");
        qt.setDescription("detection call");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "present_call", "detection call");

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
    public void testGuessQuantitationTypeParameters_ScaleOverride() {
        QuantitationType qt = new QuantitationType();
        qt.setName("log2_signal");
        qt.setDescription("log2 transformed signal");
        qt.setScale(ScaleType.LINEAR); // Override

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "log2_signal", "log2 transformed signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale()); // Should remain unchanged
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
    public void testGuessQuantitationTypeParameters_TypeOverride() {
        QuantitationType qt = new QuantitationType();
        qt.setName("present_call");
        qt.setDescription("detection call");
        qt.setType(StandardQuantitationType.AMOUNT); // Override

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "present_call", "detection call");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
        assertEquals(ScaleType.OTHER, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType()); // Should remain unchanged
        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_RepresentationOverride() {
        QuantitationType qt = new QuantitationType();
        qt.setName("count");
        qt.setDescription("number of features");
        qt.setRepresentation(PrimitiveType.STRING); // Override

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "count", "number of features");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.STRING, qt.getRepresentation()); // Should remain unchanged
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsBackgroundOverride() {
        QuantitationType qt = new QuantitationType();
        qt.setName("background");
        qt.setDescription("background intensity");
        qt.setIsBackground(Boolean.TRUE); // Override

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "background", "background intensity");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertTrue(qt.getIsBackground()); // Should remain unchanged
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsBackgroundSubtractedOverride() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ch1d");
        qt.setDescription("background subtracted signal");
        qt.setIsBackgroundSubtracted(Boolean.FALSE); // Override

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1d", "background subtracted signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted()); // Should remain unchanged
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsNormalizedOverride() {
        QuantitationType qt = new QuantitationType();
        qt.setName("normalized");
        qt.setDescription("normalized signal");
        qt.setIsNormalized(Boolean.FALSE); // Override

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "normalized", "normalized signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized()); // Should remain unchanged
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsRatioOverride() {
        QuantitationType qt = new QuantitationType();
        qt.setName("ratio");
        qt.setDescription("fold change ratio");
        qt.setIsRatio(Boolean.FALSE); // Override

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ratio", "fold change ratio");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio()); // Should remain unchanged
        assertFalse(qt.getIsPreferred());
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsPreferredOverride() {
        QuantitationType qt = new QuantitationType();
        qt.setName("rma");
        qt.setDescription("RMA signal");
        qt.setIsPreferred(Boolean.FALSE); // Override

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "rma", "RMA signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred()); // Should remain unchanged
        assertFalse(qt.getIsMaskedPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsMaskedPreferredOverride() {
        QuantitationType qt = new QuantitationType();
        qt.setName("signal");
        qt.setDescription("raw signal");
        qt.setIsMaskedPreferred(Boolean.TRUE); // Override

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "raw signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
        assertTrue(qt.getIsMaskedPreferred()); // Should remain unchanged
    }

}
