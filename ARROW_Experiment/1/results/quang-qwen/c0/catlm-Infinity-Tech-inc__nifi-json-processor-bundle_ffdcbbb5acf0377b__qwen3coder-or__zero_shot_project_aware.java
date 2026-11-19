package com.nifi.processor.jsonvalidator.processors.jsonprocessor;

import com.nifi.processor.jsonvalidator.processors.jsonprocessor.JsonFieldExtractorProcessor;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JsonFieldExtractorProcessor_ArrowTest {

    private JsonFieldExtractorProcessor processor;
    private TestRunner testRunner;

    @Before
    public void setUp() {
        processor = new JsonFieldExtractorProcessor();
        testRunner = TestRunners.newTestRunner(processor);
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
        String jsonString = "{\"hello\": \"world\", \"number\": 42}";
        String jsonPath = "$.hello";
        
        testRunner.setProperty(JsonFieldExtractorProcessor.JSON_STRING, jsonPath);
        testRunner.enqueue(new ByteArrayInputStream(jsonString.getBytes()));
        testRunner.run();
        
        testRunner.assertTransferCount(JsonFieldExtractorProcessor.SUCCESS, 1);
        MockFlowFile flowFile = testRunner.getTransferredFlowFiles().get(0);
        String attributeValue = flowFile.getAttribute("match");
        assertEquals("world", attributeValue);
    }

    @Test
    public void testOnTrigger_Failure_EmptyResult() throws ProcessException, IOException {
        String jsonString = "{\"hello\": \"world\"}";
        String jsonPath = "$.nonexistent";
        
        testRunner.setProperty(JsonFieldExtractorProcessor.JSON_STRING, jsonPath);
        testRunner.enqueue(new ByteArrayInputStream(jsonString.getBytes()));
        testRunner.run();
        
        testRunner.assertTransferCount(JsonFieldExtractorProcessor.FAILURE, 1);
        MockFlowFile flowFile = testRunner.getTransferredFlowFiles().get(0);
        String attributeValue = flowFile.getAttribute("match");
        assertEquals("", attributeValue);
    }

    @Test
    public void testOnTrigger_Failure_InvalidJsonPath() throws ProcessException, IOException {
        String jsonString = "{\"hello\": \"world\"}";
        String jsonPath = "$.[invalid";
        
        testRunner.setProperty(JsonFieldExtractorProcessor.JSON_STRING, jsonPath);
        testRunner.enqueue(new ByteArrayInputStream(jsonString.getBytes()));
        testRunner.run();
        
        testRunner.assertTransferCount(JsonFieldExtractorProcessor.FAILURE, 1);
        MockFlowFile flowFile = testRunner.getTransferredFlowFiles().get(0);
        String attributeValue = flowFile.getAttribute("match");
        assertEquals("", attributeValue);
    }

    @Test
    public void testOnTrigger_NoFlowFile() throws ProcessException {
        testRunner.run();
        testRunner.assertTransferCount(JsonFieldExtractorProcessor.SUCCESS, 0);
        testRunner.assertTransferCount(JsonFieldExtractorProcessor.FAILURE, 0);
    }

    @Test
    public void testOnScheduled() {
        try {
            processor.onScheduled(null);
        } catch (Exception e) {
            fail("onScheduled should not throw exception");
        }
    }

    @Test
    public void testInit() {
        ProcessorInitializationContext context = mock(ProcessorInitializationContext.class);
        processor.init(context);
        
        List<PropertyDescriptor> descriptors = processor.getSupportedPropertyDescriptors();
        assertNotNull(descriptors);
        assertEquals(1, descriptors.size());
        assertEquals(JsonFieldExtractorProcessor.JSON_STRING, descriptors.get(0));
        
        Set<Relationship> relationships = processor.getRelationships();
        assertNotNull(relationships);
        assertEquals(2, relationships.size());
        assertTrue(relationships.contains(JsonFieldExtractorProcessor.SUCCESS));
        assertTrue(relationships.contains(JsonFieldExtractorProcessor.FAILURE));
    }
}
