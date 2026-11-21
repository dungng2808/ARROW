package com.nifi.processor.jsonvalidator.processors.jsonprocessor;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonFieldExtractorProcessor_ArrowTest {

    @Test
    public void jsonStringDescriptorHasExpectedNameAndIsRequired() {
        PropertyDescriptor descriptor = JsonFieldExtractorProcessor.JSON_STRING;

        assertEquals("JSON_STRING", descriptor.getName());
        assertTrue(descriptor.isRequired());
    }

    @Test
    public void successRelationshipHasExpectedName() {
        assertEquals("SUCCESS", JsonFieldExtractorProcessor.SUCCESS.getName());
    }

    @Test
    public void failureRelationshipHasExpectedName() {
        assertEquals("FAILURE", JsonFieldExtractorProcessor.FAILURE.getName());
    }

    @Test
    public void supportedPropertyDescriptorsAreInitiallyUninitialized() {
        JsonFieldExtractorProcessor processor = new JsonFieldExtractorProcessor();

        assertEquals(null, processor.getSupportedPropertyDescriptors());
    }

    @Test
    public void relationshipsAreInitiallyUninitialized() {
        JsonFieldExtractorProcessor processor = new JsonFieldExtractorProcessor();

        assertEquals(null, processor.getRelationships());
    }
}
