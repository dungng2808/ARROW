package com.addhen.serializer;

import org.junit.Test;
import static org.junit.Assert.*;
import com.addhen.serializer.gson.GsonSerializationStrategyFactory;

public class Serializer_ArrowTest {

    @Test
    public void testBuilderDefaultConstructor() {
        Serializer.Builder builder = new Serializer.Builder();
        assertNotNull(builder);
    }

    @Test
    public void testBuilderWithFactory() {
        SerializationStrategy.Factory factory = GsonSerializationStrategyFactory.create();
        Serializer.Builder builder = new Serializer.Builder(factory);
        assertNotNull(builder);
    }

    @Test
    public void testBuilderSetSerializationStrategyFactory() {
        SerializationStrategy.Factory factory = GsonSerializationStrategyFactory.create();
        Serializer.Builder builder = new Serializer.Builder();
        Serializer.Builder result = builder.setSerializationStrategyFactory(factory);
        assertSame(builder, result);
    }

    @Test(expected = NullPointerException.class)
    public void testBuilderSetSerializationStrategyFactoryNull() {
        Serializer.Builder builder = new Serializer.Builder();
        builder.setSerializationStrategyFactory(null);
    }

    @Test
    public void testBuilderBuild() {
        SerializationStrategy.Factory factory = GsonSerializationStrategyFactory.create();
        Serializer.Builder builder = new Serializer.Builder(factory);
        Serializer serializer = builder.build();
        assertNotNull(serializer);
    }

    @Test
    public void testSerializationStrategyFactory() {
        SerializationStrategy.Factory factory = GsonSerializationStrategyFactory.create();
        Serializer serializer = new Serializer(factory);
        SerializationStrategy.Factory result = serializer.serializationStrategyFactory();
        assertSame(factory, result);
    }

    @Test
    public void testStrategy() {
        SerializationStrategy.Factory factory = GsonSerializationStrategyFactory.create();
        Serializer serializer = new Serializer(factory);
        SerializationStrategy<String> result = serializer.strategy(String.class);
        assertNotNull(result);
    }
}
