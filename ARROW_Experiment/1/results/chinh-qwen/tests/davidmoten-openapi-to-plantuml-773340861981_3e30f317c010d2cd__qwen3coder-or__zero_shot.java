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
    public void testOpenApiToPumlSplitByMethodWithSimpleInput() throws IOException {
        String openApi = "{\n" +
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
        
        InputStream in = new ByteArrayInputStream(openApi.getBytes(StandardCharsets.UTF_8));
        List<PumlExtract> extracts = Converter.openApiToPumlSplitByMethod(in);
        
        assertEquals(1, extracts.size());
        PumlExtract extract = extracts.get(0);
        String puml = extract.puml();
        assertTrue(puml.contains("-->"));
    }
    
    @Test
    public void testOpenApiToPumlWithSimpleInput() throws IOException {
        String openApi = "{\n" +
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
        
        InputStream in = new ByteArrayInputStream(openApi.getBytes(StandardCharsets.UTF_8));
        String result = Converter.openApiToPuml(in);
        
        assertTrue(result.contains("@startuml"));
        assertTrue(result.contains("-->"));
    }
}
