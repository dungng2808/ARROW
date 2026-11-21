package com.github.davidmoten.oas3.puml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class Converter_ArrowTest {

    @Test
    public void convertsEmptyOpenApiDocument() throws Exception {
        String openApi = "openapi: 3.0.0\ninfo:\n  title: Example\n  version: 1.0.0\npaths: {}\n";
        String result = Converter.openApiToPuml(
                new ByteArrayInputStream(openApi.getBytes(StandardCharsets.UTF_8)));

        assertEquals("@startuml"
                + "\nhide <<Path>> circle"
                + "\nhide <<Response>> circle"
                + "\nhide <<Parameter>> circle"
                + "\nhide empty methods"
                + "\nhide empty fields"
                + "\nskinparam class {"
                + "\nBackgroundColor<<Path>> Wheat"
                + "\n}"
                + "\nset namespaceSeparator none"
                + "\n\n@enduml", result);
    }

    @Test
    public void rejectsInputThatIsNotAnOpenApiDefinition() {
        try {
            Converter.openApiToPuml("not an OpenAPI definition");
        } catch (IllegalArgumentException e) {
            assertEquals("Not an OpenAPI definition", e.getMessage());
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    public void convertsOpenApiWithPath() {
        String openApi = "openapi: 3.0.0\n"
                + "info:\n  title: Example\n  version: 1.0.0\n"
                + "paths:\n"
                + "  /hello:\n"
                + "    get:\n"
                + "      responses:\n"
                + "        '200':\n"
                + "          description: OK\n";

        String result = Converter.openApiToPuml(openApi);

        assertTrue(result.startsWith("@startuml"));
        assertTrue(result.contains("class \"/hello\" <<Path>>"));
        assertTrue(result.endsWith("\n\n@enduml"));
    }
}
