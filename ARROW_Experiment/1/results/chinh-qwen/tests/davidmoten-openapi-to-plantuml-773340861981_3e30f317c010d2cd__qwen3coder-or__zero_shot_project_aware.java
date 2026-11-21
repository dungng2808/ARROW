package com.github.davidmoten.oas3.puml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import com.github.davidmoten.oas3.internal.model.PumlExtract;

public class Converter_ArrowTest {

    @Test
    public void testOpenApiToPumlWithInputStream() throws IOException {
        String openApi = "{\n" +
                "  \"openapi\": \"3.0.0\",\n" +
                "  \"info\": {\n" +
                "    \"title\": \"Test API\",\n" +
                "    \"version\": \"1.0.0\"\n" +
                "  },\n" +
                "  \"paths\": {\n" +
                "    \"/users\": {\n" +
                "      \"get\": {\n" +
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
        
        InputStream in = new ByteArrayInputStream(openApi.getBytes());
        String result = Converter.openApiToPuml(in);
        assertTrue(result.contains("@startuml"));
        assertTrue(result.contains("class \"User\" <<Response>>"));
        assertTrue(result.contains("class \"GET /users\" <<Path>>"));
    }

    @Test
    public void testOpenApiToPumlSplitByMethod() throws IOException {
        String openApi = "{\n" +
                "  \"openapi\": \"3.0.0\",\n" +
                "  \"info\": {\n" +
                "    \"title\": \"Test API\",\n" +
                "    \"version\": \"1.0.0\"\n" +
                "  },\n" +
                "  \"paths\": {\n" +
                "    \"/users\": {\n" +
                "      \"get\": {\n" +
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
        
        InputStream in = new ByteArrayInputStream(openApi.getBytes());
        List<PumlExtract> result = Converter.openApiToPumlSplitByMethod(in);
        assertEquals(1, result.size());
        assertTrue(result.get(0).puml().contains("class \"GET /users\" <<Path>>"));
    }

    @Test
    public void testOpenApiToPumlWithString() {
        String openApi = "{\n" +
                "  \"openapi\": \"3.0.0\",\n" +
                "  \"info\": {\n" +
                "    \"title\": \"Test API\",\n" +
                "    \"version\": \"1.0.0\"\n" +
                "  },\n" +
                "  \"paths\": {\n" +
                "    \"/users\": {\n" +
                "      \"get\": {\n" +
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
        
        String result = Converter.openApiToPuml(openApi);
        assertTrue(result.contains("@startuml"));
        assertTrue(result.contains("class \"User\" <<Response>>"));
        assertTrue(result.contains("class \"GET /users\" <<Path>>"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOpenApiInvalidInput() {
        String invalidOpenApi = "invalid json";
        Converter.parseOpenApi(invalidOpenApi);
    }
}
