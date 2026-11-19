package uk.gov.gchq.koryphe.serialisation.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SimpleClassSerializer_ArrowTest {
    @Test
    public void serializeWritesSimpleClassName() throws Exception {
        SimpleClassSerializer serializer = new SimpleClassSerializer();
        StringWriter output = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(output);
        SerializerProvider provider = new ObjectMapper().getSerializerProvider();

        serializer.serialize(String.class, generator, provider);
        generator.close();

        assertEquals("\"String\"", output.toString());
    }

    @Test
    public void getModuleReturnsModule() {
        assertNotNull(SimpleClassSerializer.getModule());
    }
}
