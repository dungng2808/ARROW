package step.functions.handler;

import org.junit.Test;
import step.core.reports.Measure;
import step.functions.io.Output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class FunctionMessageHandler_ArrowTest {

    @Test
    public void customMeasureDataContainsKeywordType() {
        FunctionMessageHandler handler = new FunctionMessageHandler();

        Map<String, Object> data = handler.customMeasureData();

        Map<String, Object> expected = new HashMap<>();
        expected.put(MeasureTypes.ATTRIBUTE_TYPE, MeasureTypes.TYPE_KEYWORD);
        assertEquals(expected, data);
    }

    @Test
    public void addsCustomTypeToMeasuresAndPreservesExistingData() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Measure measure = new Measure();
        Map<String, Object> originalData = new HashMap<>();
        originalData.put("name", "value");
        measure.setData(originalData);
        List<Measure> measures = new ArrayList<>();
        measures.add(measure);

        handler.addCustomTypeToOutputMeasures(measures);

        Map<String, Object> expected = new HashMap<>();
        expected.put("name", "value");
        expected.put(MeasureTypes.ATTRIBUTE_TYPE, MeasureTypes.TYPE_CUSTOM);
        assertEquals(expected, measure.getData());
    }

    @Test
    public void preservesExistingMeasureType() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Measure measure = new Measure();
        Map<String, Object> originalData = new HashMap<>();
        originalData.put(MeasureTypes.ATTRIBUTE_TYPE, "existing");
        measure.setData(originalData);
        List<Measure> measures = new ArrayList<>();
        measures.add(measure);

        handler.addCustomTypeToOutputMeasures(measures);

        assertEquals("existing", measure.getData().get(MeasureTypes.ATTRIBUTE_TYPE));
    }

    @Test
    public void handlesNullMeasureList() {
        FunctionMessageHandler handler = new FunctionMessageHandler();

        handler.addCustomTypeToOutputMeasures(null);
    }

    @Test
    public void addsAdditionalMeasuresWhenOutputHasNoMeasures() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Output<Object> output = new Output<>();
        Measure additionalMeasure = new Measure();
        List<Measure> additionalMeasures = new ArrayList<>();
        additionalMeasures.add(additionalMeasure);

        handler.addAdditionalMeasuresToOutput(output, additionalMeasures);

        assertEquals(additionalMeasures, output.getMeasures());
    }

    @Test
    public void appendsAdditionalMeasuresWhenOutputAlreadyHasMeasures() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Output<Object> output = new Output<>();
        Measure existingMeasure = new Measure();
        List<Measure> existingMeasures = new ArrayList<>();
        existingMeasures.add(existingMeasure);
        output.setMeasures(existingMeasures);

        Measure additionalMeasure = new Measure();
        List<Measure> additionalMeasures = new ArrayList<>();
        additionalMeasures.add(additionalMeasure);

        handler.addAdditionalMeasuresToOutput(output, additionalMeasures);

        assertEquals(2, output.getMeasures().size());
        assertEquals(existingMeasure, output.getMeasures().get(0));
        assertEquals(additionalMeasure, output.getMeasures().get(1));
    }

    @Test
    public void customTypeIsAddedWhenMeasureDataIsNull() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Measure measure = new Measure();
        List<Measure> measures = new ArrayList<>();
        measures.add(measure);

        handler.addCustomTypeToOutputMeasures(measures);

        assertNotNull(measure.getData());
        assertEquals(MeasureTypes.TYPE_CUSTOM,
                measure.getData().get(MeasureTypes.ATTRIBUTE_TYPE));
    }

    @Test
    public void emptyAdditionalMeasuresLeaveExistingMeasuresUnchanged() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Output<Object> output = new Output<>();
        List<Measure> existingMeasures = new ArrayList<>();
        Measure existingMeasure = new Measure();
        existingMeasures.add(existingMeasure);
        output.setMeasures(existingMeasures);

        handler.addAdditionalMeasuresToOutput(output, new ArrayList<Measure>());

        assertEquals(1, output.getMeasures().size());
        assertEquals(existingMeasure, output.getMeasures().get(0));
    }
}
