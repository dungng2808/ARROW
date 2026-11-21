package uk.gov.gchq.koryphe.serialisation.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class SimpleClassSerializer_ArrowTest {

    @Mock
    private JsonGenerator jsonGenerator;

    @Mock
    private SerializerProvider serializerProvider;

    @Test
    public void shouldCreateModuleWithCorrectSerializers() {
        // When
        SimpleModule module = SimpleClassSerializer.getModule();

        // Then
        assertNotNull(module);
    }

    @Test
    public void shouldSerializeClassToSimpleClassName() throws IOException {
        // Given
        MockitoAnnotations.initMocks(this);
        SimpleClassSerializer serializer = new SimpleClassSerializer();
        Class<?> testClass = String.class;

        // When
        serializer.serialize(testClass, jsonGenerator, serializerProvider);

        // Then
        verify(jsonGenerator).writeString("java.lang.String");
    }

    @Test
    public void shouldHandleNullClass() throws IOException {
        // Given
        MockitoAnnotations.initMocks(this);
        SimpleClassSerializer serializer = new SimpleClassSerializer();

        // When
        serializer.serialize(null, jsonGenerator, serializerProvider);

        // Then
        verify(jsonGenerator).writeString("null");
    }
}
