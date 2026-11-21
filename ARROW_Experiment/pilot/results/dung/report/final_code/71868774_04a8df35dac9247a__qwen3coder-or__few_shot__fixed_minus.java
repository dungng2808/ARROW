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
        Serializer.Builder builder = new Serializer.Builder();
        assertNotNull(builder);
    }

    @Test
    public void testBuilderSetSerializationStrategyFactory() {
        Serializer.Builder builder = new Serializer.Builder();
        Serializer.Builder result = builder.setSerializationStrategyFactory(GsonSerializationStrategyFactory.create());
        assertSame(builder, result);
    }

    @Test(expected = NullPointerException.class)
    public void testBuilderSetSerializationStrategyFactoryNull() {
        Serializer.Builder builder = new Serializer.Builder();
        builder.setSerializationStrategyFactory(null);
    }

    @Test
    public void testBuilderBuild() {
        Serializer.Builder builder = new Serializer.Builder();
        Serializer serializer = builder.build();
        assertNotNull(serializer);
    }

    @Test
    public void testSerializationStrategyFactory() {
        Serializer serializer = new Serializer(null);
        SerializationStrategy.Factory result = serializer.serializationStrategyFactory();
        assertNull(result);
    }

    @Test
    public void testStrategy() {
        Serializer serializer = new Serializer(GsonSerializationStrategyFactory.create());
        SerializationStrategy<String> result = serializer.strategy(String.class);
        assertNotNull(result);
    }
}
