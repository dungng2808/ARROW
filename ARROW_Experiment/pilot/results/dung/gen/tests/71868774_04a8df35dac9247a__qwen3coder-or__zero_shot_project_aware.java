package com.addhen.serializer;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class Serializer_ArrowTest {

    @Test
    public void testConstructor() {
        SerializationStrategy.Factory mockFactory = mock(SerializationStrategy.Factory.class);
        Serializer serializer = new Serializer(mockFactory);
        assertEquals(mockFactory, serializer.serializationStrategyFactory());
    }

    @Test
    public void testStrategy() {
        SerializationStrategy.Factory mockFactory = mock(SerializationStrategy.Factory.class);
        SerializationStrategy<String> mockStrategy = mock(SerializationStrategy.class);
        when(mockFactory.strategy(String.class)).thenReturn(mockStrategy);

        Serializer serializer = new Serializer(mockFactory);
        SerializationStrategy<String> result = serializer.strategy(String.class);
        assertEquals(mockStrategy, result);
    }

    @Test
    public void testBuilderDefaultConstructor() {
        Serializer.Builder builder = new Serializer.Builder();
        Serializer serializer = builder.build();
        assertNotNull(serializer);
    }

    @Test
    public void testBuilderWithFactory() {
        SerializationStrategy.Factory mockFactory = mock(SerializationStrategy.Factory.class);
        Serializer.Builder builder = new Serializer.Builder(mockFactory);
        Serializer serializer = builder.build();
        assertEquals(mockFactory, serializer.serializationStrategyFactory());
    }

    @Test
    public void testBuilderSetSerializationStrategyFactory() {
        SerializationStrategy.Factory mockFactory1 = mock(SerializationStrategy.Factory.class);
        SerializationStrategy.Factory mockFactory2 = mock(SerializationStrategy.Factory.class);
        Serializer.Builder builder = new Serializer.Builder(mockFactory1);
        Serializer.Builder result = builder.setSerializationStrategyFactory(mockFactory2);
        assertEquals(builder, result);
        Serializer serializer = builder.build();
        assertEquals(mockFactory2, serializer.serializationStrategyFactory());
    }

    @Test(expected = NullPointerException.class)
    public void testBuilderSetSerializationStrategyFactoryNull() {
        Serializer.Builder builder = new Serializer.Builder();
        builder.setSerializationStrategyFactory(null);
    }

    @Test
    public void testBuilderBuild() {
        SerializationStrategy.Factory mockFactory = mock(SerializationStrategy.Factory.class);
        Serializer.Builder builder = new Serializer.Builder(mockFactory);
        Serializer serializer = builder.build();
        assertNotNull(serializer);
        assertEquals(mockFactory, serializer.serializationStrategyFactory());
    }
}
