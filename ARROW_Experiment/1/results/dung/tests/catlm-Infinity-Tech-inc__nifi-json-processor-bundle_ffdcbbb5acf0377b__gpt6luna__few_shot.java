package com.nifi.processor.jsonvalidator.processors.jsonprocessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Set;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;
import org.junit.Test;

public class JsonFieldExtractorProcessor_ArrowTest {

    @Test
    public void jsonStringDescriptorHasExpectedNameAndIsRequired() {
        PropertyDescriptor descriptor = JsonFieldExtractorProcessor.JSON_STRING;

        assertEquals("JSON_STRING", descriptor.getName());
        assertEquals("JSON String to validate", descriptor.getDisplayName());
        assertEquals(true, descriptor.isRequired());
    }

    @Test
    public void initializationProvidesSupportedDescriptorAndRelationships() {
        JsonFieldExtractorProcessor processor = new JsonFieldExtractorProcessor();
        processor.initialize(null);

        List<PropertyDescriptor> descriptors = processor.getSupportedPropertyDescriptors();
        assertEquals(1, descriptors.size());
        assertEquals(JsonFieldExtractorProcessor.JSON_STRING, descriptors.get(0));

        Set<Relationship> relationships = processor.getRelationships();
        assertNotNull(relationships);
        assertEquals(2, relationships.size());
        assertEquals(true, relationships.contains(JsonFieldExtractorProcessor.SUCCESS));
        assertEquals(true, relationships.contains(JsonFieldExtractorProcessor.FAILURE));
    }
}
