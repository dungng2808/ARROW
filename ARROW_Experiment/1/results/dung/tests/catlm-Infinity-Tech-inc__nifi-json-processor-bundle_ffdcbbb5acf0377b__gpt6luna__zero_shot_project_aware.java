package com.nifi.processor.jsonvalidator.processors.jsonprocessor;

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonFieldExtractorProcessor_ArrowTest {

    @Test
    public void extractsConfiguredJsonPathAndTransfersToSuccess() {
        JsonFieldExtractorProcessor processor = new JsonFieldExtractorProcessor();
        TestRunner runner = TestRunners.newTestRunner(processor);
        runner.setProperty(JsonFieldExtractorProcessor.JSON_STRING, "$.name");
        runner.enqueue("{\"name\":\"Alice\"}");

        runner.run();

        assertEquals(1, runner.getFlowFilesForRelationship(JsonFieldExtractorProcessor.SUCCESS).size());
        assertEquals("Alice", runner.getFlowFilesForRelationship(JsonFieldExtractorProcessor.SUCCESS)
                .get(0).getAttribute("match"));
        assertEquals("Alice", runner.getContentAsString(
                runner.getFlowFilesForRelationship(JsonFieldExtractorProcessor.SUCCESS).get(0)));
    }

    @Test
    public void transfersEmptyJsonPathResultToFailure() {
        JsonFieldExtractorProcessor processor = new JsonFieldExtractorProcessor();
        TestRunner runner = TestRunners.newTestRunner(processor);
        runner.setProperty(JsonFieldExtractorProcessor.JSON_STRING, "$.missing");
        runner.enqueue("{\"name\":\"Alice\"}");

        runner.run();

        assertEquals(1, runner.getFlowFilesForRelationship(JsonFieldExtractorProcessor.FAILURE).size());
        assertEquals("", runner.getFlowFilesForRelationship(JsonFieldExtractorProcessor.FAILURE)
                .get(0).getAttribute("match"));
        assertEquals("", runner.getContentAsString(
                runner.getFlowFilesForRelationship(JsonFieldExtractorProcessor.FAILURE).get(0)));
    }
}
