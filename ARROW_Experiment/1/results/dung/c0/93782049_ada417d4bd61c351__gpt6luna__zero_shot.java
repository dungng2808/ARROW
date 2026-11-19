package org.csstudio.apputil.formula.array;

import org.epics.util.array.ArrayDouble;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VType;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ArrayDivisionFunction_ArrowTest {

    @Test
    public void getNameReturnsArrayDiv() {
        ArrayDivisionFunction function = new ArrayDivisionFunction();

        assertEquals("arrayDiv", function.getName());
    }

    @Test
    public void getDescriptionReturnsDescription() {
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
        VNumberArray first = VNumberArray.of(ArrayDouble.of(8.0, 9.0), null, null, null);
        VNumberArray second = VNumberArray.of(ArrayDouble.of(2.0, 3.0), null, null, null);

        VType result = function.compute(first, second);

        assertTrue(result instanceof VNumberArray);
        assertEquals(Arrays.asList(4.0, 3.0), ((VNumberArray) result).getData());
    }

    @Test
    public void computeReturnsNanArrayForNonNumericInputs() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();

        VType result = function.compute(null, null);

        assertNotNull(result);
        assertTrue(result instanceof VNumberArray);
    }

    @Test(expected = Exception.class)
    public void computeRejectsArraysOfDifferentLengths() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        VNumberArray first = VNumberArray.of(ArrayDouble.of(8.0, 9.0), null, null, null);
        VNumberArray second = VNumberArray.of(ArrayDouble.of(2.0), null, null, null);

        function.compute(first, second);
    }
}
