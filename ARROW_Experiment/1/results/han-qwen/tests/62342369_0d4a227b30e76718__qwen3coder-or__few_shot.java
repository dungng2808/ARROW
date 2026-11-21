package step.functions.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import step.core.reports.Measure;
import step.core.reports.MeasurementsBuilder;
import step.functions.io.Input;
import step.functions.io.Output;
import step.grid.agent.handler.context.OutputMessageBuilder;
import step.grid.agent.tokenpool.AgentTokenWrapper;
import step.grid.contextbuilder.ApplicationContextBuilder;
import step.grid.filemanager.FileVersionId;
import step.grid.io.InputMessage;
import step.grid.io.OutputMessage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FunctionMessageHandler_ArrowTest {

    private FunctionMessageHandler functionMessageHandler;
    private AgentTokenWrapper token;
    private InputMessage inputMessage;
    private Map<String, String> properties;
    private FileVersionId fileVersionId;

    @Before
    public void setUp() {
        functionMessageHandler = new FunctionMessageHandler();
        token = mock(AgentTokenWrapper.class);
        inputMessage = mock(InputMessage.class);
        properties = new HashMap<>();
        fileVersionId = mock(FileVersionId.class);
        
        when(inputMessage.getProperties()).thenReturn(properties);
    }

    @Test
    public void testAddCustomTypeToOutputMeasures_NullMeasures() {
        // Test with null measures list
        functionMessageHandler.addCustomTypeToOutputMeasures(null);
        // Should not throw any exception
    }

    @Test
    public void testAddCustomTypeToOutputMeasures_EmptyMeasures() {
        // Test with empty measures list
        List<Measure> measures = new java.util.ArrayList<>();
        functionMessageHandler.addCustomTypeToOutputMeasures(measures);
        // Should not throw any exception
    }

    @Test
    public void testAddCustomTypeToOutputMeasures_MeasuresWithoutData() {
        // Test with measures that have no data
        Measure measure = new Measure("test", 0, 0, null);
        List<Measure> measures = new java.util.ArrayList<>();
        measures.add(measure);
        
        functionMessageHandler.addCustomTypeToOutputMeasures(measures);
        
        assertNotNull(measure.getData());
        assertEquals("custom", measure.getData().get("type"));
    }

    @Test
    public void testAddCustomTypeToOutputMeasures_MeasuresWithData() {
        // Test with measures that have existing data
        Map<String, Object> data = new HashMap<>();
        data.put("existing", "value");
        Measure measure = new Measure("test", 0, 0, data);
        List<Measure> measures = new java.util.ArrayList<>();
        measures.add(measure);
        
        functionMessageHandler.addCustomTypeToOutputMeasures(measures);
        
        assertNotNull(measure.getData());
        assertEquals("value", measure.getData().get("existing"));
        assertEquals("custom", measure.getData().get("type"));
    }

    @Test
    public void testAddAdditionalMeasuresToOutput_NullOutputMeasures() {
        // Test with null output measures
        Output<?> output = mock(Output.class);
        when(output.getMeasures()).thenReturn(null);
        
        List<Measure> additionalMeasures = new java.util.ArrayList<>();
        additionalMeasures.add(new Measure("test", 0, 0, null));
        
        functionMessageHandler.addAdditionalMeasuresToOutput(output, additionalMeasures);
        
        verify(output).setMeasures(additionalMeasures);
    }

    @Test
    public void testAddAdditionalMeasuresToOutput_NonNullOutputMeasures() {
        // Test with non-null output measures
        Output<?> output = mock(Output.class);
        List<Measure> existingMeasures = new java.util.ArrayList<>();
        existingMeasures.add(new Measure("existing", 0, 0, null));
        when(output.getMeasures()).thenReturn(existingMeasures);
        
        List<Measure> additionalMeasures = new java.util.ArrayList<>();
        additionalMeasures.add(new Measure("additional", 0, 0, null));
        
        functionMessageHandler.addAdditionalMeasuresToOutput(output, additionalMeasures);
        
        assertEquals(2, existingMeasures.size());
        assertEquals("existing", existingMeasures.get(0).getName());
        assertEquals("additional", existingMeasures.get(1).getName());
    }

    @Test
    public void testCustomMeasureData() {
        // Test custom measure data creation
        Map<String, Object> data = functionMessageHandler.customMeasureData();
        
        assertNotNull(data);
        assertEquals("keyword", data.get("type"));
    }

    @Test
    public void testGetMergedAgentProperties_NullAgentProperties() {
        // Test with null agent properties
        when(token.getServices()).thenReturn(mock(step.grid.agent.AgentTokenServices.class));
        when(token.getServices().getAgentProperties()).thenReturn(null);
        when(token.getProperties()).thenReturn(new HashMap<>());
        
        Map<String, String> merged = functionMessageHandler.getMergedAgentProperties(token);
        
        assertNotNull(merged);
        assertEquals(0, merged.size());
    }

    @Test
    public void testGetMergedAgentProperties_NullTokenProperties() {
        // Test with null token properties
        when(token.getServices()).thenReturn(mock(step.grid.agent.AgentTokenServices.class));
        when(token.getServices().getAgentProperties()).thenReturn(new HashMap<>());
        when(token.getProperties()).thenReturn(null);
        
        Map<String, String> merged = functionMessageHandler.getMergedAgentProperties(token);
        
        assertNotNull(merged);
        assertEquals(0, merged.size());
    }

    @Test
    public void testGetMergedAgentProperties_BothPropertiesNotNull() {
        // Test with both properties not null
        Map<String, String> agentProps = new HashMap<>();
        agentProps.put("agentProp", "agentValue");
        
        Map<String, String> tokenProps = new HashMap<>();
        tokenProps.put("tokenProp", "tokenValue");
        
        when(token.getServices()).thenReturn(mock(step.grid.agent.AgentTokenServices.class));
        when(token.getServices().getAgentProperties()).thenReturn(agentProps);
        when(token.getProperties()).thenReturn(tokenProps);
        
        Map<String, String> merged = functionMessageHandler.getMergedAgentProperties(token);
        
        assertNotNull(merged);
        assertEquals(2, merged.size());
        assertEquals("agentValue", merged.get("agentProp"));
        assertEquals("tokenValue", merged.get("tokenProp"));
    }
}
