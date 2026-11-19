package step.functions.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import step.core.reports.Measure;
import step.functions.io.Output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class FunctionMessageHandler_ArrowTest {

    @Test
    public void customMeasureDataContainsKeywordType() {
        FunctionMessageHandler handler = new FunctionMessageHandler();

        Map<String, Object> data = handler.customMeasureData();

        assertEquals(1, data.size());
        assertEquals(MeasureTypes.TYPE_KEYWORD, data.get(MeasureTypes.ATTRIBUTE_TYPE));
    }

    @Test
    public void addCustomTypeToOutputMeasuresAddsCustomTypeAndPreservesData() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Measure measure = new Measure();
        Map<String, Object> originalData = new HashMap<>();
        originalData.put("name", "value");
        measure.setData(originalData);
        List<Measure> measures = new ArrayList<>();
        measures.add(measure);

        handler.addCustomTypeToOutputMeasures(measures);

        assertEquals(MeasureTypes.TYPE_CUSTOM, measure.getData().get(MeasureTypes.ATTRIBUTE_TYPE));
        assertEquals("value", measure.getData().get("name"));
        assertEquals(2, measure.getData().size());
    }

    @Test
    public void addCustomTypeToOutputMeasuresPreservesExistingType() {
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
    public void addCustomTypeToOutputMeasuresHandlesNullList() {
        FunctionMessageHandler handler = new FunctionMessageHandler();

        handler.addCustomTypeToOutputMeasures(null);
    }

    @Test
    public void addCustomTypeToOutputMeasuresHandlesNullMeasureData() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Measure measure = new Measure();
        measure.setData(null);
        List<Measure> measures = new ArrayList<>();
        measures.add(measure);

        handler.addCustomTypeToOutputMeasures(measures);

        assertNotNull(measure.getData());
        assertEquals(MeasureTypes.TYPE_CUSTOM, measure.getData().get(MeasureTypes.ATTRIBUTE_TYPE));
    }

    @Test
    public void addAdditionalMeasuresToOutputSetsMeasuresWhenInitiallyNull() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Output<Object> output = new Output<>();
        List<Measure> additionalMeasures = new ArrayList<>();
        Measure measure = new Measure();
        additionalMeasures.add(measure);

        handler.addAdditionalMeasuresToOutput(output, additionalMeasures);

        assertEquals(additionalMeasures, output.getMeasures());
        assertEquals(1, output.getMeasures().size());
    }

    @Test
    public void addAdditionalMeasuresToOutputAppendsMeasures() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Output<Object> output = new Output<>();
        List<Measure> existingMeasures = new ArrayList<>();
        Measure existing = new Measure();
        existingMeasures.add(existing);
        output.setMeasures(existingMeasures);
        List<Measure> additionalMeasures = new ArrayList<>();
        Measure additional = new Measure();
        additionalMeasures.add(additional);

        handler.addAdditionalMeasuresToOutput(output, additionalMeasures);

        assertEquals(2, output.getMeasures().size());
        assertEquals(existing, output.getMeasures().get(0));
        assertEquals(additional, output.getMeasures().get(1));
    }

    @Test
    public void addAdditionalMeasuresToOutputAllowsNullAdditionalMeasuresWhenOutputMeasuresExist() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Output<Object> output = new Output<>();
        List<Measure> existingMeasures = new ArrayList<>();
        output.setMeasures(existingMeasures);

        handler.addAdditionalMeasuresToOutput(output, null);

        assertEquals(0, output.getMeasures().size());
    }

    @Test
    public void addAdditionalMeasuresToOutputSetsNullWhenBothListsAreNull() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Output<Object> output = new Output<>();

        handler.addAdditionalMeasuresToOutput(output, null);

        assertNull(output.getMeasures());
    }
}
