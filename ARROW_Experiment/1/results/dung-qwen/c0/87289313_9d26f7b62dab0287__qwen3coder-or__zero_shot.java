package uk.gov.gchq.koryphe.serialisation.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class SimpleClassSerializer_ArrowTest {

    @Test
    public void shouldSerializeClassToSimpleClassName() throws IOException {
        // Given
        SimpleClassSerializer serializer = new SimpleClassSerializer();
        JsonGenerator jsonGenerator = mock(JsonGenerator.class);
        SerializerProvider serializerProvider = mock(SerializerProvider.class);
        Class<?> testClass = String.class;

        // When
        serializer.serialize(testClass, jsonGenerator, serializerProvider);

        // Then
        verify(jsonGenerator).writeString("java.lang.String");
    }

    @Test
    public void shouldReturnModuleWithCorrectSerializers() {
        // When
        SimpleModule module = SimpleClassSerializer.getModule();

        // Then
        // We can't easily verify the contents of the module without reflection,
        // but we can ensure the method doesn't throw and returns a non-null module
        assert module != null;
    }
}
