package com.github.davidmoten.oas3.puml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class Converter_ArrowTest {

    @Test
    public void convertsEmptyOpenApiDocument() {
        String openApi = "openapi: 3.0.0\n"
                + "info:\n"
                + "  title: Example\n"
                + "  version: 1.0.0\n"
                + "paths: {}\n";

        String expected = "@startuml"
                + "\nhide <<Path>> circle"
                + "\nhide <<Response>> circle"
                + "\nhide <<Parameter>> circle"
                + "\nhide empty methods"
                + "\nhide empty fields"
                + "\nskinparam class {"
                + "\nBackgroundColor<<Path>> Wheat"
                + "\n}"
                + "\nset namespaceSeparator none"
                + "\n\n@enduml";

        assertEquals(expected, Converter.openApiToPuml(openApi));
    }

    @Test
    public void convertsEmptyOpenApiDocumentFromInputStream() throws Exception {
        String openApi = "openapi: 3.0.0\n"
                + "info:\n"
                + "  title: Example\n"
                + "  version: 1.0.0\n"
                + "paths: {}\n";
        ByteArrayInputStream in = new ByteArrayInputStream(openApi.getBytes(StandardCharsets.UTF_8));

        String expected = "@startuml"
                + "\nhide <<Path>> circle"
                + "\nhide <<Response>> circle"
                + "\nhide <<Parameter>> circle"
                + "\nhide empty methods"
                + "\nhide empty fields"
                + "\nskinparam class {"
                + "\nBackgroundColor<<Path>> Wheat"
                + "\n}"
                + "\nset namespaceSeparator none"
                + "\n\n@enduml";

        assertEquals(expected, Converter.openApiToPuml(in));
    }

    @Test
    public void rejectsInvalidOpenApiDefinition() {
        try {
            Converter.openApiToPuml("not an OpenAPI definition");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Not an OpenAPI definition"));
            return;
        }
        throw new AssertionError("Expected an IllegalArgumentException");
    }
}
