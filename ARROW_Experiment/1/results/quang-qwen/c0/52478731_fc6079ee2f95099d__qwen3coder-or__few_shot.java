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
        assertTrue(qt.getIsPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_PresentAbsentType() {
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
        assertTrue(qt.getIsPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_ExampleValueInteger() {
        QuantitationType qt = new QuantitationType();
        qt.setName("count");
        qt.setDescription("number of features");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "count", "number of features", 42);

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.INT, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_ExampleValueString() {
        QuantitationType qt = new QuantitationType();
        qt.setName("flag");
        qt.setDescription("quality flag");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "flag", "quality flag", "PASS");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
        assertEquals(ScaleType.UNSCALED, qt.getScale());
        assertEquals(StandardQuantitationType.OTHER, qt.getType());
        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
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
    }

    @Test
    public void testGuessQuantitationTypeParameters_QualitativeDescription() {
        QuantitationType qt = new QuantitationType();
        qt.setName("category");
        qt.setDescription("qualitative category");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "category", "qualitative category");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
    }

    @Test
    public void testGuessQuantitationTypeParameters_LogScale() {
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
    }

    @Test
    public void testGuessQuantitationTypeParameters_PercentScale() {
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
    }

    @Test
    public void testGuessQuantitationTypeParameters_BackgroundType() {
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
    }

    @Test
    public void testGuessQuantitationTypeParameters_IsPreferredFalse() {
        QuantitationType qt = new QuantitationType();
        qt.setName("other");
        qt.setDescription("some other type");

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "other", "some other type");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertFalse(qt.getIsBackground());
        assertFalse(qt.getIsBackgroundSubtracted());
        assertFalse(qt.getIsNormalized());
        assertFalse(qt.getIsRatio());
        assertFalse(qt.getIsPreferred());
    }
}
