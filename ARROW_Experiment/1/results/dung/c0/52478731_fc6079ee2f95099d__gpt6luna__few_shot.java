package ubic.gemma.core.loader.expression.geo;

import org.junit.Test;
import ubic.gemma.model.common.quantitationtype.GeneralType;
import ubic.gemma.model.common.quantitationtype.PrimitiveType;
import ubic.gemma.model.common.quantitationtype.QuantitationType;
import ubic.gemma.model.common.quantitationtype.ScaleType;
import ubic.gemma.model.common.quantitationtype.StandardQuantitationType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QuantitationTypeParameterGuesser_ArrowTest {

    @Test
    public void guessesDefaultParametersForUnrecognizedName() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", null);

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
    public void guessesLogScaleAndNormalizedSignal() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "rma", "log2 normalized signal");

        assertEquals(ScaleType.LOG2, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(Boolean.TRUE, qt.getIsNormalized());
        assertEquals(Boolean.TRUE, qt.getIsBackgroundSubtracted());
        assertEquals(Boolean.TRUE, qt.getIsPreferred());
    }

    @Test
    public void guessesCategoricalPresentAbsentCall() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "abs_call", null);

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
        assertEquals(ScaleType.OTHER, qt.getScale());
        assertEquals(StandardQuantitationType.PRESENTABSENT, qt.getType());
        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
    }

    @Test
    public void guessesCoordinateAsInteger() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "array_row", null);

        assertEquals(StandardQuantitationType.COORDINATE, qt.getType());
        assertEquals(PrimitiveType.INT, qt.getRepresentation());
    }

    @Test
    public void exampleValueDeterminesRepresentation() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", null, Integer.valueOf(7));

        assertEquals(PrimitiveType.INT, qt.getRepresentation());
    }

    @Test
    public void preservesParametersAlreadySet() {
        QuantitationType qt = new QuantitationType();
        qt.setScale(ScaleType.LOG10);
        qt.setType(StandardQuantitationType.CORRELATION);
        qt.setRepresentation(PrimitiveType.STRING);
        qt.setIsBackground(Boolean.TRUE);
        qt.setIsBackgroundSubtracted(Boolean.TRUE);
        qt.setIsNormalized(Boolean.TRUE);
        qt.setIsRatio(Boolean.TRUE);
        qt.setIsPreferred(Boolean.TRUE);
        qt.setIsMaskedPreferred(Boolean.TRUE);

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", null);

        assertEquals(ScaleType.LOG10, qt.getScale());
        assertEquals(StandardQuantitationType.CORRELATION, qt.getType());
        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
        assertEquals(Boolean.TRUE, qt.getIsBackground());
        assertEquals(Boolean.TRUE, qt.getIsBackgroundSubtracted());
        assertEquals(Boolean.TRUE, qt.getIsNormalized());
        assertEquals(Boolean.TRUE, qt.getIsRatio());
        assertEquals(Boolean.TRUE, qt.getIsPreferred());
        assertEquals(Boolean.TRUE, qt.getIsMaskedPreferred());
    }

    @Test
    public void guessesRatioFromDescription() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "fold change ratio");

        assertTrue(qt.getIsRatio());
    }

    @Test
    public void qualitativeDescriptionSetsCategoricalGeneralType() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "unknown", "qualitative measurement");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
    }
}
