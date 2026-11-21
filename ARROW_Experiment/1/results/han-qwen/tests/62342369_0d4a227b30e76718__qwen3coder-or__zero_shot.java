package step.functions.handler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import step.core.reports.Measure;
import step.core.reports.MeasurementsBuilder;
import step.functions.io.Input;
import step.functions.io.Output;
import step.grid.agent.AgentTokenServices;
import step.grid.agent.handler.context.OutputMessageBuilder;
import step.grid.agent.tokenpool.AgentTokenWrapper;
import step.grid.contextbuilder.ApplicationContextBuilder;
import step.grid.filemanager.FileVersionId;
import step.grid.io.InputMessage;
import step.grid.io.OutputMessage;

public class FunctionMessageHandler_ArrowTest {

	private FunctionMessageHandler functionMessageHandler;
	private AgentTokenServices agentTokenServices;
	private AgentTokenWrapper agentTokenWrapper;
	private InputMessage inputMessage;
	private FileVersionId fileVersionId;
	private ObjectMapper objectMapper;
	private ApplicationContextBuilder applicationContextBuilder;
	private FunctionHandlerFactory functionHandlerFactory;
	private AbstractFunctionHandler abstractFunctionHandler;

	@Before
	public void setUp() {
		functionMessageHandler = new FunctionMessageHandler();
		agentTokenServices = mock(AgentTokenServices.class);
		agentTokenWrapper = mock(AgentTokenWrapper.class);
		inputMessage = mock(InputMessage.class);
		fileVersionId = mock(FileVersionId.class);
		objectMapper = mock(ObjectMapper.class);
		applicationContextBuilder = mock(ApplicationContextBuilder.class);
		functionHandlerFactory = mock(FunctionHandlerFactory.class);
		abstractFunctionHandler = mock(AbstractFunctionHandler.class);
		
		// Mock the dependencies
		when(agentTokenWrapper.getServices()).thenReturn(agentTokenServices);
		when(agentTokenServices.getFileManagerClient()).thenReturn(null);
		when(inputMessage.getProperties()).thenReturn(new HashMap<>());
		when(inputMessage.getPayload()).thenReturn(mock(ObjectNode.class));
		
		// Set up the field values
		functionMessageHandler.functionHandlerFactory = functionHandlerFactory;
		functionMessageHandler.applicationContextBuilder = applicationContextBuilder;
		functionMessageHandler.mapper = objectMapper;
	}

	@Test
	public void testInit() throws Exception {
		functionMessageHandler.init(agentTokenServices);
		
		assertNotNull(functionMessageHandler.applicationContextBuilder);
		assertNotNull(functionMessageHandler.functionHandlerFactory);
	}

	@Test
	public void testHandleWithNullFunctionPackage() throws Exception {
		when(inputMessage.getProperties()).thenReturn(new HashMap<>());
		when(applicationContextBuilder.runInContext(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(functionHandlerFactory.create(any(), anyString(), anyString(), anyString(), anyMap())).thenReturn(abstractFunctionHandler);
		
		OutputMessage result = functionMessageHandler.handle(agentTokenWrapper, inputMessage);
		
		assertNotNull(result);
	}

	@Test
	public void testAddCustomTypeToOutputMeasuresWithNullMeasures() {
		functionMessageHandler.addCustomTypeToOutputMeasures(null);
		// Should not throw any exception
	}

	@Test
	public void testAddCustomTypeToOutputMeasuresWithEmptyMeasures() {
		List<Measure> measures = java.util.Collections.emptyList();
		functionMessageHandler.addCustomTypeToOutputMeasures(measures);
		// Should not throw any exception
	}

	@Test
	public void testAddAdditionalMeasuresToOutputWithNullOutputMeasures() {
		Output<?> output = mock(Output.class);
		List<Measure> additionalMeasures = java.util.Collections.singletonList(mock(Measure.class));
		
		when(output.getMeasures()).thenReturn(null);
		
		functionMessageHandler.addAdditionalMeasuresToOutput(output, additionalMeasures);
		
		verify(output).setMeasures(additionalMeasures);
	}

	@Test
	public void testAddAdditionalMeasuresToOutputWithNonEmptyOutputMeasures() {
		Output<?> output = mock(Output.class);
		List<Measure> additionalMeasures = java.util.Collections.singletonList(mock(Measure.class));
		List<Measure> existingMeasures = java.util.Collections.singletonList(mock(Measure.class));
		
		when(output.getMeasures()).thenReturn(existingMeasures);
		
		functionMessageHandler.addAdditionalMeasuresToOutput(output, additionalMeasures);
		
		verify(output).getMeasures();
		verify(output).setMeasures(anyList());
	}

	@Test
	public void testCustomMeasureData() {
		Map<String, Object> result = functionMessageHandler.customMeasureData();
		
		assertNotNull(result);
		assertEquals("keyword", result.get(MeasureTypes.ATTRIBUTE_TYPE));
	}

	@Test
	public void testGetMergedAgentPropertiesWithNullAgentPropertiesAndNullTokenProperties() {
		Map<String, String> agentProperties = null;
		Map<String, String> tokenProperties = null;
		
		when(agentTokenWrapper.getServices()).thenReturn(agentTokenServices);
		when(agentTokenServices.getAgentProperties()).thenReturn(agentProperties);
		when(agentTokenWrapper.getProperties()).thenReturn(tokenProperties);
		
		Map<String, String> result = functionMessageHandler.getMergedAgentProperties(agentTokenWrapper);
		
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}

	@Test
	public void testGetMergedAgentPropertiesWithNonNullAgentPropertiesAndNullTokenProperties() {
		Map<String, String> agentProperties = new HashMap<>();
		agentProperties.put("key1", "value1");
		Map<String, String> tokenProperties = null;
		
		when(agentTokenWrapper.getServices()).thenReturn(agentTokenServices);
		when(agentTokenServices.getAgentProperties()).thenReturn(agentProperties);
		when(agentTokenWrapper.getProperties()).thenReturn(tokenProperties);
		
		Map<String, String> result = functionMessageHandler.getMergedAgentProperties(agentTokenWrapper);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("value1", result.get("key1"));
	}

	@Test
	public void testGetMergedAgentPropertiesWithNullAgentPropertiesAndNonNullTokenProperties() {
		Map<String, String> agentProperties = null;
		Map<String, String> tokenProperties = new HashMap<>();
		tokenProperties.put("key2", "value2");
		
		when(agentTokenWrapper.getServices()).thenReturn(agentTokenServices);
		when(agentTokenServices.getAgentProperties()).thenReturn(agentProperties);
		when(agentTokenWrapper.getProperties()).thenReturn(tokenProperties);
		
		Map<String, String> result = functionMessageHandler.getMergedAgentProperties(agentTokenWrapper);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("value2", result.get("key2"));
	}

	@Test
	public void testGetMergedAgentPropertiesWithNonNullAgentPropertiesAndNonNullTokenProperties() {
		Map<String, String> agentProperties = new HashMap<>();
		agentProperties.put("key1", "value1");
		Map<String, String> tokenProperties = new HashMap<>();
		tokenProperties.put("key2", "value2");
		
		when(agentTokenWrapper.getServices()).thenReturn(agentTokenServices);
		when(agentTokenServices.getAgentProperties()).thenReturn(agentProperties);
		when(agentTokenWrapper.getProperties()).thenReturn(tokenProperties);
		
		Map<String, String> result = functionMessageHandler.getMergedAgentProperties(agentTokenWrapper);
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("value1", result.get("key1"));
		assertEquals("value2", result.get("key2"));
	}
}
