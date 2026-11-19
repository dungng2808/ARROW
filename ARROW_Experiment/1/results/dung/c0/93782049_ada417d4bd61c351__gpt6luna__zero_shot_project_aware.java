package org.csstudio.apputil.formula.array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.epics.util.array.ArrayDouble;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VType;
import org.junit.Test;

public class ArrayDivisionFunction_ArrowTest {

    @Test
    public void getNameReturnsArrayDiv() {
        ArrayDivisionFunction function = new ArrayDivisionFunction();

        assertEquals("arrayDiv", function.getName());
    }

    @Test
    public void getDescriptionReturnsFormula() {
        ArrayDivisionFunction function = new ArrayDivisionFunction();

        assertEquals("Result[x] = array1[x] / array2[x].", function.getDescription());
    }

    @Test
    public void getArgumentsReturnsTwoArrays() {
        ArrayDivisionFunction function = new ArrayDivisionFunction();

        assertEquals(Arrays.asList("array", "array"), function.getArguments());
    }

    @Test
    public void computeDividesCorrespondingElements() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        VNumberArray numerator = VNumberArray.of(
                ArrayDouble.of(8.0, 9.0, 10.0),
                org.epics.vtype.Alarm.none(),
                org.epics.vtype.Time.now(),
                org.epics.vtype.Display.none());
        VNumberArray denominator = VNumberArray.of(
                ArrayDouble.of(2.0, 3.0, 5.0),
                org.epics.vtype.Alarm.none(),
                org.epics.vtype.Time.now(),
                org.epics.vtype.Display.none());

        VNumberArray result = (VNumberArray) function.compute(numerator, denominator);

        assertEquals(Arrays.asList(4.0, 3.0, 2.0), result.getData());
    }

    @Test
    public void computeReturnsDefaultForNonNumericArrays() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();

        VType result = function.compute(null, null);

        assertSame(BaseArrayFunction.DEFAULT_NAN_DOUBLE_ARRAY, result);
    }

    @Test
    public void computeThrowsForDifferentArrayLengths() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        VNumberArray numerator = VNumberArray.of(
                ArrayDouble.of(8.0, 9.0),
                org.epics.vtype.Alarm.none(),
                org.epics.vtype.Time.now(),
                org.epics.vtype.Display.none());
        VNumberArray denominator = VNumberArray.of(
                ArrayDouble.of(2.0),
                org.epics.vtype.Alarm.none(),
                org.epics.vtype.Time.now(),
                org.epics.vtype.Display.none());

        try {
            function.compute(numerator, denominator);
            fail("Expected an exception for arrays of different lengths");
        } catch (Exception ex) {
            assertEquals("Function arrayDiv cannot compute as specified arrays are of different length", ex.getMessage());
        }
    }
}
