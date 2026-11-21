package com.addhen.serializer;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class Serializer_ArrowTest {

    @Test
    public void testBuilderDefaultConstructor() {
        Serializer.Builder builder = new Serializer.Builder();
        assertNotNull(builder);
    }

    @Test
    public void testBuilderWithFactory() {
        SerializationStrategy.Factory mockFactory = mock(SerializationStrategy.Factory.class);
        Serializer.Builder builder = new Serializer.Builder(mockFactory);
        assertNotNull(builder);
    }

    @Test
    public void testBuilderSetSerializationStrategyFactory() {
        SerializationStrategy.Factory mockFactory = mock(SerializationStrategy.Factory.class);
        Serializer.Builder builder = new Serializer.Builder();
        Serializer.Builder result = builder.setSerializationStrategyFactory(mockFactory);
        assertSame(builder, result);
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
    }

    @Test
    public void testSerializationStrategyFactory() {
        SerializationStrategy.Factory mockFactory = mock(SerializationStrategy.Factory.class);
        Serializer serializer = new Serializer(mockFactory);
        SerializationStrategy.Factory result = serializer.serializationStrategyFactory();
        assertSame(mockFactory, result);
    }

    @Test
    public void testStrategy() {
        SerializationStrategy.Factory mockFactory = mock(SerializationStrategy.Factory.class);
        SerializationStrategy<String> mockStrategy = mock(SerializationStrategy.class);
        when(mockFactory.strategy(String.class)).thenReturn(mockStrategy);
        
        Serializer serializer = new Serializer(mockFactory);
        SerializationStrategy<String> result = serializer.strategy(String.class);
        
        assertSame(mockStrategy, result);
        verify(mockFactory).strategy(String.class);
    }
}
