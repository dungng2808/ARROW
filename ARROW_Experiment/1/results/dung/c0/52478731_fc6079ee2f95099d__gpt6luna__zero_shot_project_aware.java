package ubic.gemma.core.loader.expression.geo;

import org.junit.Test;
import ubic.gemma.core.loader.expression.geo.model.GeoValues;
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
    public void guessesDefaultsForOrdinarySignal() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "ordinary signal");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
        assertEquals(Boolean.FALSE, qt.getIsBackground());
        assertEquals(Boolean.FALSE, qt.getIsBackgroundSubtracted());
        assertEquals(Boolean.FALSE, qt.getIsNormalized());
        assertEquals(Boolean.FALSE, qt.getIsRatio());
        assertEquals(Boolean.TRUE, qt.getIsPreferred());
        assertEquals(Boolean.FALSE, qt.getIsMaskedPreferred());
    }

    @Test
    public void guessesCategoricalPresentAbsentCall() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "present call", "");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
        assertEquals(ScaleType.OTHER, qt.getScale());
        assertEquals(StandardQuantitationType.PRESENTABSENT, qt.getType());
        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
    }

    @Test
    public void guessesCoordinateAsInteger() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "array_row", "");

        assertEquals(GeneralType.QUANTITATIVE, qt.getGeneralType());
        assertEquals(StandardQuantitationType.COORDINATE, qt.getType());
        assertEquals(PrimitiveType.INT, qt.getRepresentation());
    }

    @Test
    public void specialProbeIdIsCategoricalAndUnscaled() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "Probe ID", "");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
        assertEquals(ScaleType.UNSCALED, qt.getScale());
        assertEquals(StandardQuantitationType.OTHER, qt.getType());
    }

    @Test
    public void qualitativeDescriptionOverridesGeneralType() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "qualitative signal");

        assertEquals(GeneralType.CATEGORICAL, qt.getGeneralType());
    }

    @Test
    public void preservesExistingQuantitationParameters() {
        QuantitationType qt = new QuantitationType();
        qt.setScale(ScaleType.LOG2);
        qt.setType(StandardQuantitationType.CORRELATION);
        qt.setRepresentation(PrimitiveType.INT);
        qt.setIsBackground(Boolean.TRUE);
        qt.setIsBackgroundSubtracted(Boolean.TRUE);
        qt.setIsNormalized(Boolean.TRUE);
        qt.setIsRatio(Boolean.TRUE);
        qt.setIsPreferred(Boolean.FALSE);
        qt.setIsMaskedPreferred(Boolean.TRUE);

        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "");

        assertEquals(ScaleType.LOG2, qt.getScale());
        assertEquals(StandardQuantitationType.CORRELATION, qt.getType());
        assertEquals(PrimitiveType.INT, qt.getRepresentation());
        assertEquals(Boolean.TRUE, qt.getIsBackground());
        assertEquals(Boolean.TRUE, qt.getIsBackgroundSubtracted());
        assertEquals(Boolean.TRUE, qt.getIsNormalized());
        assertEquals(Boolean.TRUE, qt.getIsRatio());
        assertEquals(Boolean.FALSE, qt.getIsPreferred());
        assertEquals(Boolean.TRUE, qt.getIsMaskedPreferred());
    }

    @Test
    public void exampleValueDeterminesPrimitiveRepresentation() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "", Integer.valueOf(7));

        assertEquals(PrimitiveType.INT, qt.getRepresentation());
    }

    @Test
    public void nonNumericExampleValueUsesStringRepresentation() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "", "not numeric");

        assertEquals(PrimitiveType.STRING, qt.getRepresentation());
    }

    @Test
    public void logDescriptionDeterminesScale() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "log2 transformed signal");

        assertEquals(ScaleType.LOG2, qt.getScale());
    }

    @Test
    public void backgroundSubtractedDescriptionIsDetected() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal",
                "signal background subtracted");

        assertEquals(Boolean.TRUE, qt.getIsBackgroundSubtracted());
        assertEquals(Boolean.FALSE, qt.getIsBackground());
    }

    @Test
    public void ratioDescriptionIsDetected() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", "fold change ratio");

        assertEquals(Boolean.TRUE, qt.getIsRatio());
    }

    @Test
    public void nullDescriptionIsHandled() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "signal", null);

        assertNotNull(qt.getScale());
        assertNotNull(qt.getType());
        assertNotNull(qt.getRepresentation());
    }

    @Test
    public void emptyNameIsHandled() {
        QuantitationType qt = new QuantitationType();
        QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, "", "");

        assertEquals(StandardQuantitationType.AMOUNT, qt.getType());
        assertEquals(ScaleType.LINEAR, qt.getScale());
        assertEquals(PrimitiveType.DOUBLE, qt.getRepresentation());
    }

    @Test
    public void nullNameThrowsNullPointerException() {
        QuantitationType qt = new QuantitationType();
        try {
            QuantitationTypeParameterGuesser.guessQuantitationTypeParameters(qt, null, "");
        } catch (NullPointerException expected) {
            assertTrue(true);
            return;
        }
        throw new AssertionError("Expected NullPointerException");
    }
}
