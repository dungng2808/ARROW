package com.nifi.processor.jsonvalidator.processors.jsonprocessor;

import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.components.PropertyValue;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import static org.junit.Assert.*;
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
        String jsonString = "{\"hello\": \"world\"}";
        String jsonPath = "$.hello";
        String expectedValue = "world";

        when(context.getProperty(JsonFieldExtractorProcessor.JSON_STRING)).thenReturn(propertyValue);
        when(propertyValue.getValue()).thenReturn(jsonPath);
        when(session.get()).thenReturn(flowFile);
        when(context.getProperty(JsonFieldExtractorProcessor.JSON_STRING)).thenReturn(propertyValue);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doAnswer(invocation -> {
            OutputStream out = invocation.getArgument(1);
            out.write(jsonString.getBytes());
            return null;
        }).when(session).read(eq(flowFile), any());

        doAnswer(invocation -> {
            OutputStream out = invocation.getArgument(1);
            out.write(expectedValue.getBytes());
            return null;
        }).when(session).write(eq(flowFile), any());

        // Execute
        processor.onTrigger(context, session);

        // Verify
        verify(session).putAttribute(eq(flowFile), eq("match"), eq(expectedValue));
        verify(session).transfer(eq(flowFile), eq(JsonFieldExtractorProcessor.SUCCESS));
    }

    @Test
    public void testOnTrigger_Failure_EmptyResult() throws ProcessException, IOException {
        // Setup
        String jsonString = "{\"hello\": \"world\"}";
        String jsonPath = "$.nonexistent";
        String expectedValue = "";

        when(context.getProperty(JsonFieldExtractorProcessor.JSON_STRING)).thenReturn(propertyValue);
        when(propertyValue.getValue()).thenReturn(jsonPath);
        when(session.get()).thenReturn(flowFile);
        when(context.getProperty(JsonFieldExtractorProcessor.JSON_STRING)).thenReturn(propertyValue);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doAnswer(invocation -> {
            OutputStream out = invocation.getArgument(1);
            out.write(jsonString.getBytes());
            return null;
        }).when(session).read(eq(flowFile), any());

        doAnswer(invocation -> {
            OutputStream out = invocation.getArgument(1);
            out.write(expectedValue.getBytes());
            return null;
        }).when(session).write(eq(flowFile), any());

        // Execute
        processor.onTrigger(context, session);

        // Verify
        verify(session).putAttribute(eq(flowFile), eq("match"), eq(""));
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
        assertNotNull(descriptors);
        assertEquals(1, descriptors.size());
        assertEquals(JsonFieldExtractorProcessor.JSON_STRING, descriptors.get(0));
    }

    @Test
    public void testGetRelationships() {
        // Execute
        Set<Relationship> relationships = processor.getRelationships();

        // Verify
        assertNotNull(relationships);
        assertEquals(2, relationships.size());
        assertTrue(relationships.contains(JsonFieldExtractorProcessor.SUCCESS));
        assertTrue(relationships.contains(JsonFieldExtractorProcessor.FAILURE));
    }
}
