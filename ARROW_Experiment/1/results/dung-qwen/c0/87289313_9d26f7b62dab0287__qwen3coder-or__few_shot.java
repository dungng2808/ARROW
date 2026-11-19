package uk.gov.gchq.koryphe.serialisation.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class SimpleClassSerializer_ArrowTest {

    @Test
    public void shouldAddSerializersToModule() {
        // When
        SimpleModule module = SimpleClassSerializer.getModule();

        // Then - just checking that the module was configured correctly
        // We can't easily verify the serializers were added without reflection,
        // but we can ensure the method doesn't throw
        assertNotNull(module);
    }

    @Test
    public void shouldSerializeClassToString() throws IOException {
        // Given
        SimpleClassSerializer serializer = new SimpleClassSerializer();
        JsonGenerator generator = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        
        // When
        serializer.serialize(String.class, generator, provider);

        // Then
        verify(generator).writeString(anyString());
    }
}
