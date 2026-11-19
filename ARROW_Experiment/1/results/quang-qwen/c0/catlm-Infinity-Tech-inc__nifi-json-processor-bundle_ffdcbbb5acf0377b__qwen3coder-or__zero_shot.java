package com.nifi.processor.jsonvalidator.processors.jsonprocessor;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JsonFieldExtractorProcessor_ArrowTest {

    @Mock
    private ProcessContext context;

    @Mock
    private ProcessSession session;

    @Mock
    private FlowFile flowFile;

    @Mock
    private PropertyValue propertyValue;

    private JsonFieldExtractorProcessor processor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        processor = new JsonFieldExtractorProcessor();
    }

    @Test
    public void testGetSupportedPropertyDescriptors() {
        List<PropertyDescriptor> descriptors = processor.getSupportedPropertyDescriptors();
        assertNotNull(descriptors);
        assertEquals(1, descriptors.size());
        assertEquals(JsonFieldExtractorProcessor.JSON_STRING, descriptors.get(0));
    }

    @Test
    public void testGetRelationships() {
        Set<Relationship> relationships = processor.getRelationships();
        assertNotNull(relationships);
        assertEquals(2, relationships.size());
        assertTrue(relationships.contains(JsonFieldExtractorProcessor.SUCCESS));
        assertTrue(relationships.contains(JsonFieldExtractorProcessor.FAILURE));
    }

    @Test
    public void testOnTrigger_Success() throws ProcessException, IOException {
        // Setup mocks
        when(context.getProperty(JsonFieldExtractorProcessor.JSON_STRING)).thenReturn(propertyValue);
        when(propertyValue.getValue()).thenReturn("$.name");
        when(session.get()).thenReturn(flowFile);
        when(flowFile.getAttribute("match")).thenReturn(null);

        // Mock the input stream to return valid JSON
        final String jsonContent = "{\"name\":\"John\"}";
        when(session.read(any(FlowFile.class), any(InputStreamCallback.class))).thenAnswer(invocation -> {
            InputStreamCallback callback = invocation.getArgument(1);
            callback.process(new ByteArrayInputStream(jsonContent.getBytes()));
            return null;
        });

        // Mock the output stream
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doAnswer(invocation -> {
            OutputStreamCallback callback = invocation.getArgument(1);
            callback.process(baos);
            return null;
        }).when(session).write(any(FlowFile.class), any());

        // Execute
        processor.onTrigger(context, session);

        // Verify
        verify(session).putAttribute(any(FlowFile.class), eq("match"), eq("John"));
        verify(session).transfer(any(FlowFile.class), eq(JsonFieldExtractorProcessor.SUCCESS));
    }

    @Test
    public void testOnTrigger_Failure_EmptyResult() throws ProcessException, IOException {
        // Setup mocks
        when(context.getProperty(JsonFieldExtractorProcessor.JSON_STRING)).thenReturn(propertyValue);
        when(propertyValue.getValue()).thenReturn("$.nonexistent");
        when(session.get()).thenReturn(flowFile);
        when(flowFile.getAttribute("match")).thenReturn(null);

        // Mock the input stream to return valid JSON
        final String jsonContent = "{\"name\":\"John\"}";
        when(session.read(any(FlowFile.class), any(InputStreamCallback.class))).thenAnswer(invocation -> {
            InputStreamCallback callback = invocation.getArgument(1);
            callback.process(new ByteArrayInputStream(jsonContent.getBytes()));
            return null;
        });

        // Mock the output stream
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doAnswer(invocation -> {
            OutputStreamCallback callback = invocation.getArgument(1);
            callback.process(baos);
            return null;
        }).when(session).write(any(FlowFile.class), any());

        // Execute
        processor.onTrigger(context, session);

        // Verify
        verify(session).putAttribute(any(FlowFile.class), eq("match"), eq(""));
        verify(session).transfer(any(FlowFile.class), eq(JsonFieldExtractorProcessor.FAILURE));
    }

    @Test
    public void testOnTrigger_NoFlowFile() throws ProcessException {
        when(session.get()).thenReturn(null);

        processor.onTrigger(context, session);

        verifyZeroInteractions(context);
    }

    @Test
    public void testOnTrigger_InvalidJson() throws ProcessException, IOException {
        // Setup mocks
        when(context.getProperty(JsonFieldExtractorProcessor.JSON_STRING)).thenReturn(propertyValue);
        when(propertyValue.getValue()).thenReturn("$.name");
        when(session.get()).thenReturn(flowFile);

        // Mock the input stream to return invalid JSON
        final String invalidJsonContent = "{invalid json}";
        when(session.read(any(FlowFile.class), any(InputStreamCallback.class))).thenAnswer(invocation -> {
            InputStreamCallback callback = invocation.getArgument(1);
            callback.process(new ByteArrayInputStream(invalidJsonContent.getBytes()));
            return null;
        });

        // Mock the output stream
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doAnswer(invocation -> {
            OutputStreamCallback callback = invocation.getArgument(1);
            callback.process(baos);
            return null;
        }).when(session).write(any(FlowFile.class), any());

        // Execute
        processor.onTrigger(context, session);

        // Verify
        verify(session).putAttribute(any(FlowFile.class), eq("match"), eq(""));
        verify(session).transfer(any(FlowFile.class), eq(JsonFieldExtractorProcessor.FAILURE));
    }
}
