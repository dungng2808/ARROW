package com.nifi.processor.jsonvalidator.processors.jsonprocessor;

import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.components.PropertyValue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class JsonFieldExtractorProcessor_ArrowTest {

    private JsonFieldExtractorProcessor processor;

    @Mock
    private ProcessContext context;

    @Mock
    private ProcessSession session;

    @Mock
    private FlowFile flowFile;

    @Mock
    private PropertyValue propertyValue;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        processor = new JsonFieldExtractorProcessor();
    }

    @Test
    public void testOnTrigger_Success() throws ProcessException, IOException {
        // Setup
        String jsonString = "{\"hello\": \"world\", \"number\": 42}";
        String jsonPath = "$.hello";
        String expectedValue = "world";

        when(context.getProperty(JsonFieldExtractorProcessor.JSON_STRING)).thenReturn(propertyValue);
        when(propertyValue.getValue()).thenReturn(jsonPath);
        when(session.get()).thenReturn(flowFile);
        when(context.getProperty(JsonFieldExtractorProcessor.JSON_STRING)).thenReturn(propertyValue);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doAnswer(invocation -> {
            OutputStreamCallback callback = invocation.getArgument(1);
            callback.process(baos);
            return null;
        }).when(session).write(any(FlowFile.class), any(OutputStreamCallback.class));

        doAnswer(invocation -> {
            FlowFile ff = invocation.getArgument(0);
            String attribute = invocation.getArgument(1);
            String value = invocation.getArgument(2);
            return ff;
        }).when(session).putAttribute(any(FlowFile.class), anyString(), anyString());

        doAnswer(invocation -> {
            FlowFile ff = invocation.getArgument(0);
            return ff;
        }).when(session).transfer(any(FlowFile.class), any());

        // Mock the InputStreamCallback behavior
        doAnswer(invocation -> {
            InputStreamCallback callback = invocation.getArgument(1);
            callback.process(new ByteArrayInputStream(jsonString.getBytes()));
            return null;
        }).when(session).read(any(FlowFile.class), any(InputStreamCallback.class));

        // Execute
        processor.onTrigger(context, session);

        // Verify
        verify(session).read(eq(flowFile), any(InputStreamCallback.class));
        verify(session).putAttribute(eq(flowFile), eq("match"), eq(expectedValue));
        verify(session).write(eq(flowFile), any(OutputStreamCallback.class));
        verify(session).transfer(eq(flowFile), eq(JsonFieldExtractorProcessor.SUCCESS));
    }

    @Test
    public void testOnTrigger_Failure_InvalidJsonPath() throws ProcessException, IOException {
        // Setup
        String jsonString = "{\"hello\": \"world\"}";
        String jsonPath = "$.invalid.field";
        String expectedValue = "";

        when(context.getProperty(JsonFieldExtractorProcessor.JSON_STRING)).thenReturn(propertyValue);
        when(propertyValue.getValue()).thenReturn(jsonPath);
        when(session.get()).thenReturn(flowFile);
        when(context.getProperty(JsonFieldExtractorProcessor.JSON_STRING)).thenReturn(propertyValue);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doAnswer(invocation -> {
            OutputStreamCallback callback = invocation.getArgument(1);
            callback.process(baos);
            return null;
        }).when(session).write(any(FlowFile.class), any(OutputStreamCallback.class));

        doAnswer(invocation -> {
            FlowFile ff = invocation.getArgument(0);
            String attribute = invocation.getArgument(1);
            String value = invocation.getArgument(2);
            return ff;
        }).when(session).putAttribute(any(FlowFile.class), anyString(), anyString());

        doAnswer(invocation -> {
            FlowFile ff = invocation.getArgument(0);
            return ff;
        }).when(session).transfer(any(FlowFile.class), any());

        // Mock the InputStreamCallback behavior
        doAnswer(invocation -> {
            InputStreamCallback callback = invocation.getArgument(1);
            callback.process(new ByteArrayInputStream(jsonString.getBytes()));
            return null;
        }).when(session).read(any(FlowFile.class), any(InputStreamCallback.class));

        // Execute
        processor.onTrigger(context, session);

        // Verify
        verify(session).read(eq(flowFile), any(InputStreamCallback.class));
        verify(session).putAttribute(eq(flowFile), eq("match"), eq(expectedValue));
        verify(session).write(eq(flowFile), any(OutputStreamCallback.class));
        verify(session).transfer(eq(flowFile), eq(JsonFieldExtractorProcessor.FAILURE));
    }

    @Test
    public void testOnTrigger_NoFlowFile() throws ProcessException {
        // Setup
        when(session.get()).thenReturn(null);

        // Execute
        processor.onTrigger(context, session);

        // Verify
        verifyZeroInteractions(context);
        verifyZeroInteractions(session);
    }

    @Test
    public void testGetSupportedPropertyDescriptors() {
        // Execute
        List<PropertyDescriptor> descriptors = processor.getSupportedPropertyDescriptors();

        // Verify
        assertEquals(1, descriptors.size());
        assertEquals(JsonFieldExtractorProcessor.JSON_STRING, descriptors.get(0));
    }

    @Test
    public void testGetRelationships() {
        // Execute
        Set<Relationship> relationships = processor.getRelationships();

        // Verify
        assertEquals(2, relationships.size());
        assertTrue(relationships.contains(JsonFieldExtractorProcessor.SUCCESS));
        assertTrue(relationships.contains(JsonFieldExtractorProcessor.FAILURE));
    }
}
