package org.csstudio.apputil.formula.array;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListMath;
import org.epics.vtype.*;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class ArrayDivisionFunction_ArrowTest {

    @Test
    public void testGetName() {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        assertThat(function.getName(), is("arrayDiv"));
    }

    @Test
    public void testGetDescription() {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        assertThat(function.getDescription(), is("Result[x] = array1[x] / array2[x]."));
    }

    @Test
    public void testGetArguments() {
        ArrayDivisionFunction function = new ArrayDivisionFunction();
        List<String> arguments = function.getArguments();
        assertThat(arguments.size(), is(2));
        assertThat(arguments.get(0), is("array"));
        assertThat(arguments.get(1), is("array"));
    }

    @Test
    public void testComputeWithEqualLengthArrays() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();

        ArrayDouble array1Data = new ArrayDouble(Arrays.asList(10.0, 20.0, 30.0));
        ArrayDouble array2Data = new ArrayDouble(Arrays.asList(2.0, 4.0, 5.0));

        VNumberArray array1 = VNumberArray.of(array1Data, Alarm.none(), Time.now(), Display.none());
        VNumberArray array2 = VNumberArray.of(array2Data, Alarm.none(), Time.now(), Display.none());

        VType result = function.compute(array1, array2);

        assertThat(result, is(VNumberArray.of(
                ListMath.divide(array1Data, array2Data),
                Alarm.none(),
                Time.now(),
                Display.none()
        )));
    }

    @Test(expected = Exception.class)
    public void testComputeWithDifferentLengthArrays() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();

        ArrayDouble array1Data = new ArrayDouble(Arrays.asList(10.0, 20.0, 30.0));
        ArrayDouble array2Data = new ArrayDouble(Arrays.asList(2.0, 4.0));

        VNumberArray array1 = VNumberArray.of(array1Data, Alarm.none(), Time.now(), Display.none());
        VNumberArray array2 = VNumberArray.of(array2Data, Alarm.none(), Time.now(), Display.none());

        function.compute(array1, array2);
    }

    @Test
    public void testComputeWithNonNumericArrays() throws Exception {
        ArrayDivisionFunction function = new ArrayDivisionFunction();

        VString stringArray1 = VString.of("hello", Alarm.none(), Time.now());
        VString stringArray2 = VString.of("world", Alarm.none(), Time.now());

        VType result = function.compute(stringArray1, stringArray2);

        assertThat(result, is(BaseArrayFunction.DEFAULT_NAN_DOUBLE_ARRAY));
    }
}
