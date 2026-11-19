package uk.gov.gchq.koryphe.serialisation.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SimpleClassSerializer_ArrowTest {

    @Test
    public void serializeWritesSimpleClassName() throws IOException {
        SimpleClassSerializer serializer = new SimpleClassSerializer();
        JsonGenerator generator = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);

        serializer.serialize(String.class, generator, provider);

        verify(generator).writeString("String");
    }

    @Test
    public void getModuleReturnsSimpleModule() {
        SimpleModule module = SimpleClassSerializer.getModule();

        assertEquals(SimpleModule.class, module.getClass());
    }
}
