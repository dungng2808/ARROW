package org.csstudio.apputil.formula.array;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.epics.util.array.ListNumber;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VType;
import org.junit.Test;

public class ArrayDivisionFunction_ArrowTest {

    @Test
    public void returnsFunctionMetadata() {
        ArrayDivisionFunction function = new ArrayDivisionFunction();

        assertEquals("arrayDiv", function.getName());
        assertEquals("Result[x] = array1[x] / array2[x].", function.getDescription());
        assertEquals(Arrays.asList("array", "array"), function.getArguments());
    }

    @Test
    public void dividesCorrespondingArrayElements() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        VNumberArray first = VNumberArray.of(Arrays.asList(8.0, 9.0), null, null, null);
        VNumberArray second = VNumberArray.of(Arrays.asList(2.0, 3.0), null, null, null);

        VType result = function.compute(first, second);

        assertTrue(result instanceof VNumberArray);
        ListNumber data = ((VNumberArray) result).getData();
        assertEquals(2, data.size());
        assertEquals(4.0, data.getDouble(0), 0.0);
        assertEquals(3.0, data.getDouble(1), 0.0);
    }

    @Test
    public void returnsDefaultForNonNumericInputs() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();

        VType result = function.compute(null, null);

        assertNotNull(result);
        assertEquals(BaseArrayFunction.DEFAULT_NAN_DOUBLE_ARRAY, result);
    }

    @Test
    public void throwsWhenArrayLengthsDiffer() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        VNumberArray first = VNumberArray.of(Arrays.asList(8.0, 9.0), null, null, null);
        VNumberArray second = VNumberArray.of(Arrays.asList(2.0), null, null, null);

        try {
            function.compute(first, second);
        } catch (Exception exception) {
            assertEquals("Function arrayDiv cannot compute as specified arrays are of different length",
                    exception.getMessage());
            return;
        }
        throw new AssertionError("Expected an exception for arrays of different lengths");
    }
}
