package com.addhen.serializer;

import org.junit.Test;
import static org.junit.Assert.*;

public class Serializer_ArrowTest {

    @Test
    public void testConstructor() {
        SerializationStrategy.Factory factory = new GsonSerializationStrategyFactory();
        Serializer serializer = new Serializer(factory);
        assertEquals(factory, serializer.serializationStrategyFactory());
    }

    @Test
    public void testStrategy() {
        SerializationStrategy.Factory factory = new GsonSerializationStrategyFactory();
        Serializer serializer = new Serializer(factory);
        SerializationStrategy<String> strategy = serializer.strategy(String.class);
        assertNotNull(strategy);
    }

    @Test
    public void testBuilderDefault() {
        Serializer.Builder builder = new Serializer.Builder();
        Serializer serializer = builder.build();
        assertNotNull(serializer);
    }

    @Test
    public void testBuilderWithFactory() {
        SerializationStrategy.Factory factory = new GsonSerializationStrategyFactory();
        Serializer.Builder builder = new Serializer.Builder(factory);
        Serializer serializer = builder.build();
        assertEquals(factory, serializer.serializationStrategyFactory());
    }

    @Test
    public void testBuilderSetFactory() {
        SerializationStrategy.Factory factory1 = new GsonSerializationStrategyFactory();
        SerializationStrategy.Factory factory2 = new GsonSerializationStrategyFactory();
        Serializer.Builder builder = new Serializer.Builder(factory1);
        builder.setSerializationStrategyFactory(factory2);
        Serializer serializer = builder.build();
        assertEquals(factory2, serializer.serializationStrategyFactory());
    }

    @Test(expected = NullPointerException.class)
    public void testBuilderSetFactoryNull() {
        Serializer.Builder builder = new Serializer.Builder();
        builder.setSerializationStrategyFactory(null);
    }
}
