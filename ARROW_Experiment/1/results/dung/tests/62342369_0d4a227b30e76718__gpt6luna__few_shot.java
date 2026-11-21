package step.functions.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import step.core.reports.Measure;
import step.functions.io.Output;

public class FunctionMessageHandler_ArrowTest {

    @Test
    public void customMeasureDataHasKeywordType() {
        FunctionMessageHandler handler = new FunctionMessageHandler();

        Map<String, Object> data = handler.customMeasureData();

        assertEquals(MeasureTypes.TYPE_KEYWORD, data.get(MeasureTypes.ATTRIBUTE_TYPE));
        assertEquals(1, data.size());
    }

    @Test
    public void addsCustomTypeToMeasuresWithoutData() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Measure measure = new Measure();
        List<Measure> measures = new ArrayList<>();
        measures.add(measure);

        handler.addCustomTypeToOutputMeasures(measures);

        assertNotNull(measure.getData());
        assertEquals(MeasureTypes.TYPE_CUSTOM, measure.getData().get(MeasureTypes.ATTRIBUTE_TYPE));
    }

    @Test
    public void preservesExistingMeasureDataAndType() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Measure measure = new Measure();
        Map<String, Object> originalData = new HashMap<>();
        originalData.put("label", "value");
        originalData.put(MeasureTypes.ATTRIBUTE_TYPE, "existing");
        measure.setData(originalData);
        List<Measure> measures = new ArrayList<>();
        measures.add(measure);

        handler.addCustomTypeToOutputMeasures(measures);

        assertEquals("value", measure.getData().get("label"));
        assertEquals("existing", measure.getData().get(MeasureTypes.ATTRIBUTE_TYPE));
        assertEquals(2, measure.getData().size());
    }

    @Test
    public void handlesNullMeasureList() {
        FunctionMessageHandler handler = new FunctionMessageHandler();

        handler.addCustomTypeToOutputMeasures(null);

        assertTrue(true);
    }

    @Test
    public void addsAdditionalMeasuresWhenOutputHasNoMeasures() {
        FunctionMessageHandler handler = new FunctionMessageHandler();
        Output<Object> output = new Output<>();
        List<Measure> additionalMeasures = new ArrayList<>();
        Measure measure = new Measure();
        additionalMeasures.add(measure);

        handler.addAdditionalMeasuresToOutput(output, additionalMeasures);

        assertEquals(additionalMeasures, output.getMeasures());
    }

    @Test
    public void appendsAdditionalMeasuresWhenOutputAlreadyHasMeasures() {
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
}
