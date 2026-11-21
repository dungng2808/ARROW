package uk.gov.gchq.koryphe.serialisation.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.StringWriter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SimpleClassSerializer_ArrowTest {

    @Test
    public void serializeWritesSimpleClassName() throws Exception {
        SimpleClassSerializer serializer = new SimpleClassSerializer();
        JsonGenerator generator = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);

        serializer.serialize(String.class, generator, provider);

        verify(generator).writeString("String");
    }

    @Test
    public void getModuleReturnsModule() {
        SimpleModule module = SimpleClassSerializer.getModule();

        assertNotNull(module);
    }
}
