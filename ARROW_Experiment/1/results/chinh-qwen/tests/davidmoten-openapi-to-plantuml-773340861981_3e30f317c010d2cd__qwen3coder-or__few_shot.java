package com.github.davidmoten.oas3.puml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;

import com.github.davidmoten.oas3.internal.model.PumlExtract;

public class Converter_ArrowTest {

    @Test
    public void testOpenApiToPumlSplitByMethod() throws IOException {
        String openApiContent = "{\n" +
                "  \"openapi\": \"3.0.0\",\n" +
                "  \"info\": {\n" +
                "    \"title\": \"Test API\",\n" +
                "    \"version\": \"1.0.0\"\n" +
                "  },\n" +
                "  \"paths\": {\n" +
                "    \"/users\": {\n" +
                "      \"get\": {\n" +
                "        \"summary\": \"Get users\",\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"A list of users\",\n" +
                "            \"content\": {\n" +
                "              \"application/json\": {\n" +
                "                \"schema\": {\n" +
                "                  \"type\": \"array\",\n" +
                "                  \"items\": {\n" +
                "                    \"$ref\": \"#/components/schemas/User\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"components\": {\n" +
                "    \"schemas\": {\n" +
                "      \"User\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"id\": {\n" +
                "            \"type\": \"integer\"\n" +
                "          },\n" +
                "          \"name\": {\n" +
                "            \"type\": \"string\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        
        InputStream inputStream = new ByteArrayInputStream(openApiContent.getBytes(StandardCharsets.UTF_8));
        List<PumlExtract> extracts = Converter.openApiToPumlSplitByMethod(inputStream);
        
        assertEquals(1, extracts.size());
        assertTrue(extracts.get(0).puml().contains("@startuml"));
        assertTrue(extracts.get(0).puml().contains("class \"GET /users\" <<Path>>"));
    }

    @Test
    public void testOpenApiToPuml() throws IOException {
        String openApiContent = "{\n" +
                "  \"openapi\": \"3.0.0\",\n" +
                "  \"info\": {\n" +
                "    \"title\": \"Test API\",\n" +
                "    \"version\": \"1.0.0\"\n" +
                "  },\n" +
                "  \"paths\": {\n" +
                "    \"/users\": {\n" +
                "      \"get\": {\n" +
                "        \"summary\": \"Get users\",\n" +
                "        \"responses\": {\n" +
                "          \"200\": {\n" +
                "            \"description\": \"A list of users\",\n" +
                "            \"content\": {\n" +
                "              \"application/json\": {\n" +
                "                \"schema\": {\n" +
                "                  \"type\": \"array\",\n" +
                "                  \"items\": {\n" +
                "                    \"$ref\": \"#/components/schemas/User\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"components\": {\n" +
                "    \"schemas\": {\n" +
                "      \"User\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"id\": {\n" +
                "            \"type\": \"integer\"\n" +
                "          },\n" +
                "          \"name\": {\n" +
                "            \"type\": \"string\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        
        InputStream inputStream = new ByteArrayInputStream(openApiContent.getBytes(StandardCharsets.UTF_8));
        String result = Converter.openApiToPuml(inputStream);
        
        assertTrue(result.contains("@startuml"));
        assertTrue(result.contains("class \"GET /users\" <<Path>>"));
        assertTrue(result.contains("class \"User\""));
    }
}
