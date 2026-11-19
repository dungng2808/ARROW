package ubic.gemma.core.loader.expression.geo;

import org.junit.Test;
import ubic.gemma.model.common.quantitationtype.GeneralType;
import ubic.gemma.model.common.quantitationtype.PrimitiveType;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.common.quantitationtype.ScaleType;
import ubic.gemma.model.common.quantitationtype.StandardQuantitationType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QuantitationTypeParameterGuesser_ArrowTest {

    @Test
    public void guessesDefaultsForUnrecognizedName() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "unknown description");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertEquals(Boolean.FALSE, qt.getIsBackground());
        assertEquals(Boolean.FALSE, qt.getIsBackgroundSubtracted());
        assertEquals(Boolean.FALSE, qt.getIsNormalized());
        assertEquals(Boolean.FALSE, qt.getIsRatio());
        assertEquals(Boolean.FALSE, qt.getIsPreferred());
        assertEquals(Boolean.FALSE, qt.getIsMaskedPreferred());
    }

    @Test
    public void preservesExistingParameters() {
        QuantitationType qt = new QuantitationType();
        qt.setScale(ScaleType.LOG2);
        qt.setType(StandardQuantitationType.CORRELATION);
        qt.setRepresentation(PrimitiveType.INT);
        qt.setIsBackground(Boolean.TRUE);
        qt.setIsBackgroundSubtracted(Boolean.TRUE);
        qt.setIsNormalized(Boolean.TRUE);
        qt.setIsRatio(Boolean.TRUE);
        qt.setIsPreferred(Boolean.TRUE);
        qt.setIsMaskedPreferred(Boolean.TRUE);

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "unknown description");

        assertEquals(ScaleType.LOG2, qt.getScale());
        assertEquals(StandardQuantitationType.CORRELATION, qt.getType());
        assertEquals(PrimitiveType.INT, qt.getRepresentation());
        assertEquals(Boolean.TRUE, qt.getIsBackground());
        assertEquals(Boolean.TRUE, qt.getIsBackgroundSubtracted());
        assertEquals(Boolean.TRUE, qt.getIsNormalized());
        assertEquals(Boolean.TRUE, qt.getIsRatio());
        assertEquals(Boolean.TRUE, qt.getIsPreferred());
        assertEquals(Boolean.TRUE, qt.getIsMaskedPreferred());
    }

    @Test
    public void recognizesPercentAndRatioDescription() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "percentage ratio");

        assertEquals(ScaleType.PERCENT, qt.getScale());
        assertEquals(Boolean.TRUE, qt.getIsRatio());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
    }

    @Test
    public void recognizesPresentAbsentCall() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "present call", "");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
        assertEquals(ScaleType.OTHER, qt.getScale());
        assertEquals(StandardQuantitationType.PRESENTABSENT, qt.getType());
        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
    }

    @Test
    public void recognizesCoordinateName() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "array_row", "");

        assertEquals(StandardQuantitationType.COORDINATE, qt.getType());
        assertEquals(PrimitiveType.INT, qt.getRepresentation());
    }

    @Test
    public void recognizesSpecialProbeIdColumn() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "Probe ID", "");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
        assertEquals(ScaleType.UNSCALED, qt.getScale());
        assertEquals(StandardQuantitationType.OTHER, qt.getType());
    }

    @Test
    public void qualitativeDescriptionMakesTypeCategorical() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "qualitative measurement");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
    }

    @Test
    public void exampleValueDeterminesPrimitiveRepresentation() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "", Integer.valueOf(7));

        assertEquals(PrimitiveType.INT, qt.getRepresentation());
    }

    @Test
    public void recognizesStringExampleValue() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "", "not numeric");

        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
    }

    @Test
    public void recognizesNormalizedNameAndPreferredName() {
        QuantitationType qt = new QuantitationType();
        qt.setName("rma");
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "rma", "");

        assertEquals(Boolean.TRUE, qt.getIsNormalized());
        assertEquals(Boolean.TRUE, qt.getIsPreferred());
    }

    @Test
    public void nullDescriptionIsHandled() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", null);

        assertNotNull(qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
    }

    @Test
    public void recognizesBackgroundName() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "ch1b_signal", "");

        assertEquals(Boolean.TRUE, qt.getIsBackground());
    }

    @Test
    public void backgroundSubtractedDescriptionIsRecognized() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown",
                "signal with background subtracted");

        assertEquals(Boolean.TRUE, qt.getIsBackgroundSubtracted());
        assertEquals(Boolean.FALSE, qt.getIsBackground());
    }

    @Test
    public void booleanExampleValueDeterminesRepresentation() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "", Boolean.TRUE);

        assertEquals(PrimitiveType.BOOLEAN, qt.getRepresentation());
    }

    @Test
    public void doubleExampleValueDeterminesRepresentation() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "", Double.valueOf(2.5));

        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
    }

    @Test
    public void detectionDescriptionIsPresentAbsent() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "detection");

        assertEquals(StandardQuantitationType.PRESENTABSENT, qt.getType());
        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
    }

    @Test
    public void correlationDescriptionIsRecognized() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "correlation");

        assertEquals(StandardQuantitationType.CORRELATION, qt.getType());
    }

    @Test
    public void ratioDescriptionIsRecognized() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "fold change ratio");

        assertTrue(qt.getIsRatio());
    }

    @Test
    public void nonRatioDescriptionDoesNotSetRatio() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "ordinary signal");

        assertFalse(qt.getIsRatio());
    }
}
