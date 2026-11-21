package com.github.davidmoten.oas3.puml;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class Converter_ArrowTest {

    @Test
    public void convertsSimpleGetOperationToPlantUml() throws Exception {
        String openApi = "openapi: 3.0.0\n"
                + "info:\n"
                + "  title: Example\n"
                + "  version: '1.0'\n"
                + "paths:\n"
                + "  /hello:\n"
                + "    get:\n"
                + "      responses:\n"
                + "        '200':\n"
                + "          description: OK\n";

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
                + "\n\nclass \"/hello GET\" <<Path>>  {"
                + "\n}"
                + "\n\nclass \"/hello GET 200\" <<Response>>  {"
                + "\n}"
                + "\n\n\"/hello GET\" ..> \"1\" \"/hello GET 200\" : \"200\""
                + "\n\n@enduml";

        assertEquals(expected, Converter.openApiToPuml(new ByteArrayInputStream(openApi.getBytes(StandardCharsets.UTF_8))));
    }
}
