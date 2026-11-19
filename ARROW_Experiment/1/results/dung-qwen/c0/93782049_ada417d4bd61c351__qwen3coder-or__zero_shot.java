package org.csstudio.apputil.formula.array;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListMath;
import org.epics.vtype.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDivisionFunction_ArrowTest {

    @Test
    public void testGetName() {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        assertEquals("arrayDiv", function.getName());
    }

    @Test
    public void testGetDescription() {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        assertEquals("Result[x] = array1[x] / array2[x].", function.getDescription());
    }

    @Test
    public void testGetArguments() {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        assertEquals(2, function.getArguments().size());
        assertEquals("array", function.getArguments().get(0));
        assertEquals("array", function.getArguments().get(1));
    }

    @Test
    public void testComputeEqualLengthArrays() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        
        // Create two arrays with same length
        ArrayDouble array1Data = new ArrayDouble(new double[]{10.0, 20.0, 30.0});
        ArrayDouble array2Data = new ArrayDouble(new double[]{2.0, 4.0, 5.0});
        
        VNumberArray array1 = VNumberArray.of(array1Data, Alarm.none(), Time.now(), Display.none());
        VNumberArray array2 = VNumberArray.of(array2Data, Alarm.none(), Time.now(), Display.none());
        
        VType result = function.compute(array1, array2);
        
        assertTrue(result instanceof VNumberArray);
        VNumberArray resultArray = (VNumberArray) result;
        assertEquals(3, resultArray.getData().size());
        assertEquals(5.0, resultArray.getData().getDouble(0), 1e-10);
        assertEquals(5.0, resultArray.getData().getDouble(1), 1e-10);
        assertEquals(6.0, resultArray.getData().getDouble(2), 1e-10);
    }

    @Test
    public void testComputeDifferentLengthArrays() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        
        // Create two arrays with different lengths
        ArrayDouble array1Data = new ArrayDouble(new double[]{10.0, 20.0, 30.0});
        ArrayDouble array2Data = new ArrayDouble(new double[]{2.0, 4.0});
        
        VNumberArray array1 = VNumberArray.of(array1Data, Alarm.none(), Time.now(), Display.none());
        VNumberArray array2 = VNumberArray.of(array2Data, Alarm.none(), Time.now(), Display.none());
        
        try {
            function.compute(array1, array2);
            fail("Expected Exception was not thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("arrayDiv"));
        }
    }

    @Test
    public void testComputeNonNumericArrays() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        
        // Create non-numeric arrays
        VString stringArray1 = VString.of("hello", Alarm.none(), Time.now());
        VString stringArray2 = VString.of("world", Alarm.none(), Time.now());
        
        VType result = function.compute(stringArray1, stringArray2);
        
        assertTrue(result instanceof VNumberArray);
        VNumberArray resultArray = (VNumberArray) result;
        assertEquals(0, resultArray.getData().size());
    }
}
